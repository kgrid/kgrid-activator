package edu.umich.lhs.activator.adapter;

import edu.umich.lhs.activator.exception.ActivatorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.umich.lhs.activator.exception.ActivatorException;

/**
 * This dummy implementation of a PayloadAdapter does not execute the contents. Instead it simply
 * returns a dummy instance of whatever return type was specified.
 *
 * Created by grosscol on 5/19/17.
 */
public class NoopAdapter implements ServiceAdapter {

    public NoopAdapter() {}

    @Override
    public List<String> supports() {
        List<String> languages = new ArrayList<>();
        languages.add("Noop");
        return languages;
    }

    /**
     * Implementation that does nothing with the parameter or payload contents.
     *
     * @param args Map of parameters. Ignored.
     * @param code Code to execute. Ignored.
     * @param functionName Name of function in code to execute. Ignored.
     * @param returnType Class of Java object to return.
     * @return a new object of type specified by parameter clazz.
     */
    @Override
    public Object execute(Map<String, Object> args, String code, String functionName, Class returnType) throws ActivatorException {
        Object retVal;

        if(returnType==Integer.class)       retVal = 0;
        else if(returnType == Long.class)   retVal = 0L;
        else if(returnType == Float.class)  retVal = 0.0f;
        else if(returnType == Double.class) retVal = 0.0;
        else if(returnType == Map.class)    retVal = new HashMap<String, Object>();
        else retVal = defaultInitializer(returnType);

        return retVal;
    }

    /**
     * Attempt to create an instances of Class<T>
     * @param returnType Class to instantiate
     * @return freshly initialized instance of returnType.
     */
    private Object defaultInitializer(Class returnType) {
        try {
            Object retObject;
            retObject = returnType.newInstance();
            return retObject;
        } catch (IllegalAccessException | InstantiationException illEx) {
            ActivatorException otEx;
            String msg = "Could not instantiate return object for: " + returnType.toString();
            otEx = new ActivatorException(msg, illEx);
            throw (otEx);
        }
    }
}
