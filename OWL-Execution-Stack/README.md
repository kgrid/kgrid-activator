Notes : 
Currently OWL execution stack is part of ObjectTeller but we need to sepearate it as a different project like python execution stack.

OWL Object: 
We have one object for JNC7-Hypertension Guideline ( Ref: https://www.nhlbi.nih.gov/files/docs/resources/heart/phycard.pdf )
Payload of the object is in: Payload.xml
Sample input ( request body to demo OWL functionality )  is in: requestBody.xml
Expected output (for given payload and given input ) is in : result.json

No input message or output message are required. 

Test object is created at : http://dlhs-fedora-dev-a.umms.med.umich.edu:8080/ObjectTeller/object/ark:/99999/OT158

for demonstration 
1. URL: http://dlhs-fedora-dev-a.umms.med.umich.edu:8080/ObjectTeller/knowledgeObject/ark:/99999/OT158/result
2. Method : POST
3. Content-Type : application/owl+xml
4. Accept : application/json
5. Expected output 200 OK ( refer result.json)


 








 


