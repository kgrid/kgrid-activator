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

------------------------------------------------------------------------------------------------------------------------------------------------

Changing input values of the Patient

Change xml in the request body to change these values. 
e.g. HighCVDRiskFlag - 0 or 1 - 
 
    for example : 

    <owl:NamedIndividual rdf:about="http://med.umich.edu/cci/ontologies/domain-jnc7-hypertension-model-plus-individuals#patient-0001">
        <rdf:type rdf:resource="http://med.umich.edu/cci/ontologies/domain-jnc7-hypertension-model-plus-individuals#Patient"/>
        <domain-jnc7-hypertension-model-plus-individuals:hasSelfCheck rdf:resource="http://med.umich.edu/cci/ontologies/domain-jnc7-hypertension-model-plus-individuals#patientSelfCheck01"/>
        <domain-jnc7-hypertension-model-plus-individuals:CauseOfResistantHypertionVal>The patient took excess sodium during the testing period</domain-jnc7-hypertension-model-plus-individuals:CauseOfResistantHypertionVal>
        <domain-jnc7-hypertension-model-plus-individuals:ChrKidnyDisFlagVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">1</domain-jnc7-hypertension-model-plus-individuals:ChrKidnyDisFlagVal>
        <domain-jnc7-hypertension-model-plus-individuals:DiabetesFlagVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">0</domain-jnc7-hypertension-model-plus-individuals:DiabetesFlagVal>
        <domain-jnc7-hypertension-model-plus-individuals:HeartFailureFlagVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">5</domain-jnc7-hypertension-model-plus-individuals:HeartFailureFlagVal>
        <domain-jnc7-hypertension-model-plus-individuals:HighCVDRiskFlagVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">1</domain-jnc7-hypertension-model-plus-individuals:HighCVDRiskFlagVal>
        <domain-jnc7-hypertension-model-plus-individuals:InitialTherapyOptionFlagVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">3</domain-jnc7-hypertension-model-plus-individuals:InitialTherapyOptionFlagVal>
        <domain-jnc7-hypertension-model-plus-individuals:LifestyleModificationVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">1001</domain-jnc7-hypertension-model-plus-individuals:LifestyleModificationVal>
        <domain-jnc7-hypertension-model-plus-individuals:MRN-Jnc7>charles01</domain-jnc7-hypertension-model-plus-individuals:MRN-Jnc7>
        <domain-jnc7-hypertension-model-plus-individuals:PostMyoInfarFlagVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">0</domain-jnc7-hypertension-model-plus-individuals:PostMyoInfarFlagVal>
        <domain-jnc7-hypertension-model-plus-individuals:RecStrokePrevFlagVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">0</domain-jnc7-hypertension-model-plus-individuals:RecStrokePrevFlagVal>
    </owl:NamedIndividual>
    
    <owl:NamedIndividual rdf:about="http://med.umich.edu/cci/ontologies/domain-jnc7-hypertension-model-plus-individuals#patientSelfCheck01">
        <rdf:type rdf:resource="http://med.umich.edu/cci/ontologies/domain-jnc7-hypertension-model-plus-individuals#SelfCheck"/>
        <domain-jnc7-hypertension-model-plus-individuals:DiastolicBPVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">98</domain-jnc7-hypertension-model-plus-individuals:DiastolicBPVal>
        <domain-jnc7-hypertension-model-plus-individuals:SystolicBPVal rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">150</domain-jnc7-hypertension-model-plus-individuals:SystolicBPVal>
    </owl:NamedIndividual>
    
    You can use Protege to modify , analyse these files. 
    
    









 








 


