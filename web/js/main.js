/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

// Initialize data source for autocomplete.
var dataSource = new Bloodhound({
	datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
	queryTokenizer: Bloodhound.tokenizers.whitespace,
	remote: {
		url: 'autocomplete?q=%QUERY',
		wildcard: '%QUERY'
	}
});

// Initialize autocomplete
$('.typeahead').typeahead({
	hint: false,
	highlight: true,
	minLength: 2
},
{
	name: 'states',
	source: dataSource,
	display: 'data',
	limit: 50,
	templates: {
		empty: "<div class='tt-not-found'>Sorry, nothing found.</div>",
		suggestion: function(data) {
			return "<div><span class='entityType'>" + data.type.substring(0, 1) + "</span>" + data.data + "</div>";
		}
	}
});


// Autocomplete actions
$('.typeahead')
	.bind('typeahead:selected', onSelected)
	.focus()
	.keypress(function(e) {
		if(e.which == 13) {
			// Enter - autocomplete the query in the background and navigate there
			$("#result").html("<p class='message'>Loading ...</p>");
			$('.typeahead').typeahead('close');

			$.ajax('autocomplete?q=' + encodeURIComponent($('.typeahead').val()))
			.done(function(r) {
				if (r.length > 0)
					goTo(r[0].type, r[0].data);
				else
					$("#result").html("<p class='message'>Sorry, nothing found.</p>");
			})
			.fail(function(r) {
				$("#result").html("<p class='message error'>Error: " + r + "</p>");
			});
		}
		return true;
	});


// An entry was selected in autocomplete
var goToProcessing = false;
function onSelected(obj, selectedItem) {
	if (goToProcessing)
		return;

	query(selectedItem.type, selectedItem.data);
}

// Programatically set the autocomplete and navigate to the record
function goTo(type, name) {
	goToProcessing = true;
	$('.typeahead').val(name);
	goToProcessing = false;

	onSelected(null, {type: type, data: name});
}

// User opened a page from history 
var popingState = false;
window.onpopstate = function(e) {
	popingState = true;
	goTo(e.state.type, e.state.name);
	popingState = false;
};

// Query the server
function query(type, name) {
	if (!popingState)
		history.pushState({ name: name, type: type }, 'Who-owns: ' + name, '/');

	$("#result").html("<p class='message'>Loading ...</p>");

	$.ajax('query?t=' + encodeURIComponent(type) + '&q=' + encodeURIComponent(name))
	.done(function(r) {
		$("#result").html("");
		renderResponse(r);
	})
	.fail(function(r) {
		$("#result").html("<p class='message error'>Error: " + r + "</p>");
	});
}

// Owner colors, calculated in here for simplicity and cached so they do not change too often.
// It should really be server's task.
var allOwnerColors = {};

// Render the response
function renderResponse(data) {
	var resultElement = $("#result");
	resultElement.html("");

	var owners = {};
	listOwners(data, owners);
	var ownerColors = assignColors(owners);

	if (!(data instanceof Array))
		data = [data];

	// Multiple results - draw all of them, separate by <hr/>
	for (var i=0; i<data.length; ++i) {
		if (i!==0)
			$("<hr/>").appendTo(resultElement);
		var container = $("<div/>", {class: 'scopePathContainer'}).appendTo(resultElement);
		draw(container, data[i], ownerColors);
	}

	// List all owners sent in the response
	function listOwners(data, result) {
		var i;

		if (data instanceof Array) {
			for (i=0; i<data.length; ++i)
				listOwners(data[i], result);
		}
		else {
			data.owners = data.owners || [];
			for (i=0; i<data.owners.length; ++i)
				result[data.owners[i].name] = true;

			data.children = data.children || [];
			for (i=0; i<data.children.length; ++i)
				listOwners(data.children[i], result);
		}
	}

	// Assigns a color to every owner.
	function assignColors(owners) {
		var array = [];
		for (var owner in owners) {
			array.push(owner);
			delete allOwnerColors[owner];
		}
		for (owner in allOwnerColors)
			array.push(owner);
		array.sort();

		var result = {};
		for (var i=0; i<array.length; ++i)
			result[array[i]] = 'hsl(' + Math.floor(i*360/array.length) + ', 60%, 70%)';

		allOwnerColors = result;
		return result;
	}

	/**
	 * Creates the DOM structure based on the JSON data.
	 * @param  {DOM element}    parent      element, where to put the put the result
	 * @param  {Object}         data        Object from JSON data received from the server.
	 * @param  {Object}         ownerColors Map of colors.
	 */
	function draw(parent, data, ownerColors) {
		var item = $("<div/>", {
			class: 'scopePathItem',
			style: data.w ? 'flex-grow: ' + Math.floor(data.w) : ''
		});

		var nameElement = $("<div/>", { class: 'scopePathName' }).appendTo(item);
		$("<a/>", {style: 'background-color: white'})
			.html(filterName(data.name))
			.click(function() { goTo(data.type, filterName(data.path || data.name)); })
			.appendTo(nameElement);

		if (data.other)
			$("<p/>").html(data.other).appendTo(item);

		var owners = data.owners || [];
		if (owners.length > 0) {
			var ownersElement = $("<div/>", { class: 'scopePathOwners' }).appendTo(item);

			owners.sort(function(a,b) { return b.w - a.w; });
			owners.forEach(function(owner) {
				if (owner.w < 0.01)
					return;

				var percent = Math.floor(owner.w * 100);

				var o = $("<span/>", {
					class: 'owner',
					style: 'width: ' + percent + '%;' +
							'background-color: ' + ownerColors[owner.name] + '; '
				}).appendTo(ownersElement);

				$("<a/>", {style: 'background-color: ' + ownerColors[owner.name]})
					.html(filterName(owner.name) + ' (' + percent + '%)')
					.click(function() { goTo(owner.type, filterName(owner.name)); })
					.appendTo(o);
			});
		}

		data.children = data.children || [];
		if (data.children.length > 0) {
			var childrenContainer = $("<div/>", {class: 'scopePathChildren'}).appendTo(item);
			for (var i=0; i<data.children.length; ++i)
				draw(childrenContainer, data.children[i], ownerColors, data.path || data.name);
		}

		item.appendTo(parent);
	}
}


// Filters a name so it doesn't start with its entity-type identifier.
function filterName(name) {
	if (name.indexOf("team.") === 0)
		return name.substring(5);
	else if (name.indexOf("person.") === 0)
		return name.substring(7);
	else if (name.indexOf("java.") === 0)
		return name.substring(5);
	else if (name.indexOf("word.") === 0)
		return name.substring(5);
	else
		return name;
}

