package edu.umich.lhs.activator.adapter;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import edu.umich.lhs.activator.exception.ActivatorException;

/**
 * Created by grosscol on 5/24/17.
 */
public class NoopAdapterTest {
    Map<String, Object> args;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        args = new HashMap<>();
    }

    @Test
    public void executeEmptyPayload() throws Exception {
        ServiceAdapter adapter = new NoopAdapter();

        assertThat(adapter.execute(args, "", "", Object.class), instanceOf(Object.class));
    }

    @Test
    public void executeNonEmptyPayload() throws Exception {
        ServiceAdapter adapter = new NoopAdapter();
        assertThat(adapter.execute(args, "funcName()", "funcName", Object.class), instanceOf(Object.class));
    }

    @Test
    public void returnStandardTypes() throws Exception {
        ServiceAdapter adapter = new NoopAdapter();

        Class[] types = {Integer.class, Long.class, String.class,
                Float.class, Double.class, Map.class};

        for (Class type : types) {
            assertThat(adapter.execute(args, "code", "funcName", type), instanceOf(type));
        }
    }

    static class ZeroArgInit {
        ZeroArgInit(){ }
    }

    static class InitNeedsArgs{
        InitNeedsArgs(int bar){ }
    }

    @Test
    public void usingReturnTypeWithDefaultInit() throws Exception{
        ServiceAdapter adapter = new NoopAdapter();
        Class returnType = ZeroArgInit.class;

        assertThat(adapter.execute(args, "code", "funcName", returnType), instanceOf(returnType));
    }

    @Test
    public void usingReturnTypeWithoutDefaultInit() throws Exception{
        ServiceAdapter adapter = new NoopAdapter();
        Class returnType = InitNeedsArgs.class;

        expectedEx.expect(ActivatorException.class);
        expectedEx.expectMessage("Could not instantiate return object for: ");
        adapter.execute(args, "code", "funcName", returnType);
    }
}
