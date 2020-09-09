function main(inputs) {
    var stepExecutor = context.getExecutor("js-simple-v1.0/welcome");

    if (stepExecutor != null) {
        return stepExecutor.execute(inputs);
    } else {
        throw new Error("Cannot find simple js hello world ko.")
    }

}

