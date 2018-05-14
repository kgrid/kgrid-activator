function welcome(inputs){
  var contact = JSON.parse(inputs);
  return "Welcome to Knowledge Grid, " +  contact.name;
}