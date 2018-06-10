---
layout: page
navtitle: OLD
---
## Using the metadata in the result object

The result returned from a knowledge object (KO) in the Activator also includes meta data (`"metadata": {...}`) drawn from the KO itself, with title, owner, etc, and a source link.

If a library url (`library.url=...`)is configured for the Activator instance, the source link (`"source": "..."`) link will point to an instance of the knowledge object in a Knowledge Grid library. 

> If no library url is configured the source link will point to the global resolution service http://n2t.net, but if the KO has a temporary, reserved, or withdrawn ARK id, the resolution will fail. Also if the KO was uploaded manaully into the activator and no instance exists in the configured library, the source link will fail when followed.


For example: 

The link http://kgrid.med.umich.edu/library/knowledgeObject/ark:/67034/k4959x is included in the response (Opioid Use Detector). 
From a browser (a GET request with mime type text/html) this url redirects to http://kgrid.med.umich.edu/library/#/object/ark:%2F67034%2Fk4959x which is the library page for the KO.

If you use the same request http://kgrid.med.umich.edu/library/knowledgeObject/ark:/67034/k4959x with mime type ‘application/json’, say from a backend process, you get a version like this, and can use/display citation links directly:


```json
{
  "metadata": {
    "title": "Opioid Use Detector",
    "owner": "University of Michigan Department of Learning Health Sciences",
    "description": "A knowledge object that scans a medication regimen for the presence of an opioid.  This object has many potential uses, including one use as a filtering/screening mechanism when applied in the context of messaging and health information exchange at Transitions of Care. ",
    "contributors": "Kristen McGarry, Peter Boisvert, Allen Flynn",
    "keywords": "opioid, opiate, health information exchange, HIE, transition of care, transitions of care, medication",
    "published": false,
    "lastModified": 1494993600000,
    "createdOn": 1494993600000,
    "objectType": null,
    "citations": [
      {
        "citation_id": "7d/63/6c/c1/7d636cc1-8273-4639-965c-1b08ccc243fa",
        "citation_title": "Medication Use in the Transition from Hospital to Home",
        "citation_at": "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3575742/"
      },
      {
        "citation_id": "64/d9/e4/c2/64d9e4c2-c4ff-45d9-8c24-399aa46e3cfa",
        "citation_title": "Adverse Drug Events in Elderly Patients Receiving Home Health Services Following Hospital Discharge",
        "citation_at": "https://www.researchgate.net/profile/Shelly_Gray2/publication/12728552_Adverse_Drug_Events_in_Elderly_Patients_Receiving_Home_Health_Services_following_Hospital_Discharge/links/53e8d8e90cf2fb7487246e4c.pdf"
      },
      {
        "citation_id": "41/4c/7c/8c/414c7c8c-4240-4282-be97-723b93022ede",
        "citation_title": "Hospital admission and the start of benzodiazepine use",
        "citation_at": "http://www.bmj.com/content/bmj/304/6831/881.full.pdf"
      }
    ],
    "license": {
      "licenseName": "",
      "licenseLink": ""
    }
  },
  "inputMessage": null,
  "outputMessage": null,
  "payload": {
    "content": null,
    "functionName": null,
    "engineType": null
  },
  "logData": null,
  "uri": "ark:/67034/k4959x"
}
```
