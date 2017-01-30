package org.uofm.ot.executionStack.controller;

import org.junit.Test;
import org.uofm.ot.executionStack.transferObjects.Result;

import static org.junit.Assert.assertNotNull;

/**
 * Created by pboisver on 1/16/17.
 */
public class ExecutionStackControllerTest {
//    @Test
    public void calculate() throws Exception {
        // represent inputs as map<> of string, object
        // plug in a KO sample data, like in the manual on github in kgrid
        //add mock data from mockito

//        want the desired result to return an expected data
//                assert something
//                        or an error

    }

    @Test
    public void whenInputIsNullReturnError() {

        ExecutionStackController ex = new ExecutionStackController();

            Result result = ex.calculate(null, null);
            assertNotNull(result.getErrorMessage());
// i remember doing something like that, but it escapes me now. i am sure stackoverflow will answer
//        Assert.assertEquals(error,result);
    }

}