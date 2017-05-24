package org.uofm.ot.activator.adapter;

import java.util.HashMap;
import java.util.Map;
import org.uofm.ot.activator.exception.OTExecutionStackException;
import org.springframework.stereotype.Component;

/**
 * This dummy implementation of a PayloadAdapter does not execute the contents. Instead it simply
 * returns a dummy instance of whatever return type was specified.
 *
 * Created by grosscol on 5/19/17.
 */
@Component
public class NoopAdapter implements ServiceAdapter {

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
    public Object execute(Map<String, Object> args, String code, String functionName, Class returnType) throws OTExecutionStackException {
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
            OTExecutionStackException otEx;
            String msg = "Could not instantiate return object for: " + returnType.toString();
            otEx = new OTExecutionStackException(msg, illEx);
            throw (otEx);
        }
    }
}
