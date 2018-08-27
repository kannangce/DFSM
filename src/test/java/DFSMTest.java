
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

import in.kannangce.sm.DFSM;

public class DFSMTest
{

    private DFSM aDFSM;

    @Before
    public void setup() throws IOException
    {

        String aMap = "{" + "\"NEW\":{\"START\":\"STARTED\"},"
                + "\"STARTED\":{\"COMPLETE\":\"NEW\"}" + "}";

        ObjectMapper aMapper = new ObjectMapper();

        final Map<String, Map<String, String>> aFixedMap = aMapper.readValue(aMap,
                new TypeReference<Map<String, Map<String, String>>>()
                {
                });

        aDFSM = new DFSM()
        {

            @Override
            protected Map<String, Map<String, String>> getInitData()
            {
                return aFixedMap;
            }

            @Override
            protected String getDefaultState()
            {
                return "NEW";
            }
        };
    }

    
    @Test
    public void testDefaultState()
    {
        assertEquals("The default state is not correct.", aDFSM.getCurrentState(), "NEW");
    }
    
    @Test
    public void testReset()
    {
        assertEquals("Reset should set the state to default state.", aDFSM.reset(), "NEW");
    }

    @Test
    public void testPositiveTransition()
    {
        aDFSM.reset();
        assertEquals("The transition is not as expected.", aDFSM.transition("START"), "STARTED");
        assertEquals("The transition is not as expected.", aDFSM.getCurrentState(), "STARTED");
    }
    
    
    @Test(expected = IllegalStateException.class)
    public void testNegativeTransition()
    {
        aDFSM.reset();
        aDFSM.transition("COMPLETE");
    }
}
