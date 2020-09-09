const greetings = require('./greeting.json');
function welcome(inputs){
  let name = inputs.name;
  let language = inputs.language;
  return greetings.greeting[language] + " to Knowledge Grid, " + name;
}

module.exports = welcome
