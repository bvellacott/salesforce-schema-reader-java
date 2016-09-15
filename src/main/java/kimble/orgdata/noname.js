
//Don't pass in successCb if you don't want to populate metadata upon construction
var SObjectWalker = function(batchSize, successCb, failCb) {
	this.type = 'SObjectWalker';
	this.isFetchingMeta = true;
	this.batchSize = (typeof batchSize == 'undefined') ? 100 : batchSize;
	this.ignoreErrors = (typeof failCb == 'undefined') ? true : false;
	this.walkRelWithUdefNames = false;

	if(typeof successCb === 'function')
		this.populate(successCb, failCb);
};

//Define methods
SObjectWalker.prototype = {
	populate : function(successCb, failCb) {
		this.sobjectsPreMeta = [];
		this.sobjectsComplete = {};
		this.sobjNameBatches = [];
		this.callbacks = [];

		var threadCount = 0;
		var result = sforce.connection.describeGlobal();
		this.sobjectsPreMeta = result.getArray("sobjects");

		// Create batches
		for(var i = 0; i < this.sobjectsPreMeta.length;) {
			var batch = [];
			for(var j = 0; i < this.sobjectsPreMeta.length && j < this.batchSize; i++, j++)
				batch.push(this.sobjectsPreMeta[i].name);
			this.sobjNameBatches.push(batch);
		}
		
		var failureOccured = false;
		var failureHandled = false;
		var ctx = this;
		var cb = function(err) {
			if(failureHandled) 
				return;
			if(failureOccured) {
				console.log(err);
				failCb(err);
				failureHandled = true;
				return;
			}
			threadCount--;
			console.log(threadCount);
			if(threadCount <= 0) {
				ctx.isFetchingMeta = false;
				successCb();
				for(var i = 0; i < ctx.callbacks.length; i++)
					ctx.callbacks[i]();
			}
		};
		var fail = function(err) {
			if(!ctx.ignoreErrors) {
				failureOccured = true;
				failCb(err);
			}
			else
				console.log(err); // Only log the error
			cb(err);
		};
		
		// Process batches
		for (var i = 0; i < this.sobjNameBatches.length; i++) {
			console.log('Batch : ' + this.sobjNameBatches[i]);
			threadCount++;
			this.fetchObjectMeta(this.sobjNameBatches[i], cb, fail);
		}
	},
	getObjectNames : function() {
		var names = [];
		for(var name in this.sobjectsComplete)
			names.push(name);
		names.sort();
		return names;
	},
	pushCallbacks : function(cbs) {
		if(typeof cbs === 'function')
			cbs = [cbs];
		if( !jQuery.isArray(cbs) )
			throw 'The callback parameter must be either a function or an array of functions';
		if(!this.isFetchingMeta)
			for(var i = 0; i < cbs.length; i++)
				cbs[i]();
		else
			this.callbacks = this.callbacks.concat(cbs);
	},
	// Scan an array of objects and populate sobjectsComplete
	fetchObjectMeta : function(objs, success, fail) {
		var ctx = this;
		var fetchSuccess = function(objs) {
			try {
				var obj = objs[i]
				for(var i = 0; i < objs.length; i++)
					ctx.registerObject(objs[i]);
			} 
			catch(e) { fail(e); }
			finally{ success(); } // call the callback
		}
		sforce.connection.describeSObjects(objs, fetchSuccess, fail);
	},
	registerObject : function(obj) {
		this.sobjectsComplete[obj.name] = obj;
	},
	// see deepwalk fields method for visitor structure
	shallowWalkFields : function(visitor) {
		this.validateState();
		for(var objName in this.sobjectsComplete)
			if(this.shallowWalkObjectFieldsSH(this.sobjectsComplete[objName], visitor) === 'term') return 'term';
	},
	// see deepwalk fields method for visitor structure
	shallowWalkObjectFields : function(obj, visited, path, visitor) {
		this.validateState();
		if(typeof obj.fields === 'undefined') {
			console.log('The object has no fields defined');
		}
		for(var i = 0; i < obj.fields.length; i++) {
			var f = obj.fields[i];
			if(typeof f === 'undefined')
				continue;
			var subPath = path.slice(0);
			subPath.push(f);
			if(visitor.visit(f, obj, subPath, this) === 'term') return 'term';
		}
	},
	// A ShortHand (SH) method to shallow walk starting from the given object
	// see deepwalk fields method for visitor structure
	shallowWalkObjectFieldsSH : function(obj, visitor) {
		var visited = {};
		visited[obj.name] = true;
		return this.shallowWalkObjectFields(obj, visited, [obj], visitor);
	},
	// visitor must have a visit method which looks like: visitor.visit(field, object, path, walker) and it should return the string 'term' to terminate the walk
	// field : {} #fieldobject#,
	// object : {} #complete sobject meta object# 
	// path : [] #array of n objects where [0] = root object, [1]..[n-1] = relationship, [n] = field #]
	// walker : this walker #To use for subsequent visits#
	deepWalkFields : function(visitor) {
		this.validateState();
		for(var objName in this.sobjectsComplete)
			if(this.deepWalkObjectFieldsSH(this.sobjectsComplete[objName], visitor) === 'term') return 'term';
	},
	// see deepwalk fields method for visitor structure
	deepWalkObjectFields : function(obj, visited, path, visitor) {
		this.validateState();
		if(visited[obj.name] == true)
			return;
		if(typeof obj.fields === 'undefined')
			return;
		visited[obj.name] = true;

		if(path.length == 0)
			path.push(obj);
		//if(this.shallowWalkObjectFields(obj, visited, path, visitor) === 'term') return 'term';

		for(var i = 0; i < obj.fields.length; i++) {
			var f = obj.fields[i];
			if(typeof f === 'undefined')
				continue;
			var subPath = path.slice(0);
			subPath.push(f);
			if(visitor.visit(f, obj, subPath, this) === 'term') return 'term';
			else if(f.type === 'reference' && typeof this.sobjectsComplete[f.referenceTo] !== 'undefined')
				if(this.deepWalkObjectFields(this.sobjectsComplete[f.referenceTo], visited, subPath, visitor) === 'term') return 'term';
		}
//		if(typeof obj.childRelationships == 'undefined')
//			return;
//		for(var i = 0; i < obj.childRelationships.length; i++) {
//			var rel = obj.childRelationships[i];
//			if(!this.walkRelWithUdefNames && typeof rel.relationshipName === 'undefined')
//				continue;
//			var subPath = path.slice(0);
//			subPath.push(rel);
//			if(this.deepWalkObjectFields(this.sobjectsComplete[rel.childSObject], visited, subPath, visitor) === 'term') return 'term';
//		}
	},
	// A ShortHand (SH) method to deep walk starting from the given object
	// see deepwalk fields method for visitor structure
	deepWalkObjectFieldsSH : function(obj, visitor) {
		return this.deepWalkObjectFields(obj, [], [], visitor);
	},
	validateState : function() {
		if(this.isFetchingMeta)
			throw this.type + " hasn't finished fetching sobject metadata from the server";
	},
};
