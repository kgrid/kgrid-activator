function riskscore(input){
  var gender = input.gender.toLowerCase()
  var race = input.race.toLowerCase()
  var result = {riskscore:-1}
  var hasDiabetes = input.diabetes==="Yes"?1:0
  if (gender==="male" && race==="black") {
    var age = 2.469*Math.log(input.age)
    var totalcholesterol = 0.302*Math.log(input.totalcholesterol)
    var HDL = 0.307*Math.log(input.HDL)
    var systolic1 = 1.916*Math.log(input.systolic)
    var antihypertensive1 = (input.antihypertensive)
    var systolic2 = 1.809*Math.log(input.systolic)
    var antihypertensive2 = (!input.antihypertensive)
    var smoker = 0.549*input.smoker
    var diabetes = 0.645*hasDiabetes

    result.riskscore = 1 -  Math.pow(0.8954,Math.exp(age + totalcholesterol - HDL + systolic1 * antihypertensive1 + systolic2
    * antihypertensive2 + smoker + diabetes - 19.54))
 }
  if (gender.toLowerCase()==="female" && race.toLowerCase()==="black"){
    var age1 = 17.114*Math.log(input.age)
    var totalcholesterol = 0.940*Math.log(input.totalcholesterol)
    var HDL = 18.920*Math.log(input.HDL)
    var age2 = 4.475*Math.log(input.age)*Math.log(input.HDL)
    var systolic1 = 29.291*Math.log(input.systolic)*(input.antihypertensive)
    var age3 = 6.432*Math.log(input.age)*Math.log(input.systolic)*(input.antihypertensive)
    var systolic2 = 27.820*Math.log(input.systolic)*(!input.antihypertensive)
    var age4 = 6.087*Math.log(input.age)*Math.log(input.systolic)*(!input.antihypertensive)
    var smoker = 0.691*input.smoker
    var diabetes = 0.874*hasDiabetes

    result.riskscore = 1 -  Math.pow(0.9533,Math.exp(age1 + totalcholesterol - HDL + age2
    + systolic1 - age3 +systolic2 - age4 + smoker + diabetes - 86.61 ))
}
  if (gender==="male" && race==="white"){
    var age1 = 12.344*Math.log(input.age)
    var totalcholesterol1 = 11.853*Math.log(input.totalcholesterol)
    var age2 = 2.664*Math.log(input.age)
    var totalcholesterol2 = Math.log(input.totalcholesterol)
    var HDL1 = 7.990*Math.log(input.HDL)
    var age3 = 1.769*Math.log(input.age)
    var HDL2 = Math.log(input.HDL)
    var systolic1 = 1.797*Math.log(input.systolic)
    var antihypertensive1 = (input.antihypertensive)
    var systolic2 = 1.764*Math.log(input.systolic)
    var antihypertensive2 = (!input.antihypertensive)
    var smoker = 7.837*input.smoker
    var age4 = 1.795*Math.log(input.age)*input.smoker
    var diabetes = 0.658*hasDiabetes

    result.riskscore = 1 -  Math.pow(0.9144,Math.exp(age1 + totalcholesterol1 - age2 *totalcholesterol2 - HDL1 + age3 *HDL2 + systolic1 * antihypertensive1 + systolic2
    * antihypertensive2 + smoker - age4 + diabetes - 61.18))
}
  if (gender==="female" && race==="white"){
    var k1 = -29.799*Math.log(input.age)
    var k2 =4.884 * (Math.pow( Math.log(input.age),2))
    var k3 = 13.540*Math.log(input.totalcholesterol)
    var k4 = 3.114*Math.log(input.age)*Math.log(input.totalcholesterol)
    var k5 = 13.578*Math.log(input.HDL)
    var k6 = 3.149*Math.log(input.age)*Math.log(input.HDL)
    var k7 = 2.019*Math.log(input.systolic)
    var k8 = (input.antihypertensive)
    var k9 = 1.957*Math.log(input.systolic)
    var k10 = (!input.antihypertensive)
    var k11 = 7.574*input.smoker
    var k12 = 1.665*Math.log(input.age)*input.smoker
    var k13 = 0.661*hasDiabetes + 29.18

    result.riskscore = 1 -  Math.pow(0.9665,Math.exp(k1 + k2 + k3 - k4 - k5 + k6 + k7 * k8 + k9 * k10 + k11 - k12 +k13))
}
  return result
 }
