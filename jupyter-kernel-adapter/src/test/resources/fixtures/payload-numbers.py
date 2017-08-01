def numbers( params ):
  inputs = params["inputs"]
  return {"sum": sum(inputs),
          "ave": sum(inputs) / len(inputs),
          "max": max(inputs),
          "min": min(inputs)}
