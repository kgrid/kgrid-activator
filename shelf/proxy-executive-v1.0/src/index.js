async function process(inputs) {
    // Use arkid, version, path
    var hello = global.cxt.getExecutorByID("ark:/proxy/simple", "v1.0", "welcome")

    var results = {}
    if (hello != null) {
        results.message = await hello.execute(inputs)
    } else {
        results.message = "Error: KO not found."
    }
    return results;
}

module.exports = process
