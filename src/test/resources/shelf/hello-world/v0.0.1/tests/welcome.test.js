var rewire = require('rewire');

//Get load in the js
var javascript = rewire('../model/resource/welcome.js');

//Load in the function
var welcome = javascript.__get__("welcome");

// noinspection BadExpressionStatementJS
test('positive numbers', () => {

  expect(welcome({"name": "Barney Rubble"})).toBe("Welcome to Knowledge Grid, Barney Rubble");

});


