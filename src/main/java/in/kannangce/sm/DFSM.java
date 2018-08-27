package in.kannangce.sm;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * Class represents a Deterministic finite state machine. The below
 * characteristics describes the state machine, <br/>
 * <ol>
 * <li>The machine is capable being in <i><b>a</b> state</i> at any given
 * time.</li>
 * <li>At any state, the machine is capable of accepting an <i>input</i></li>
 * <li>If an input is acceptable in a state, the result is a new state.</li>
 * <li>If the input is not valid in the context of the current state, exception
 * will be thrown</li>
 * <li>The machine has finite set of discreet states and actions.</li>
 * <li>The machine will always be in a default state.</li>
 * <li>Each states accepts only a discreet number of actions, which is subset of
 * all the possible actions.</li>
 * </ol>
 * 
 * @author kannan.r
 *
 */
public abstract class DFSM
{

    private Map<String, State> indexedState = new HashMap<>();

    private State defaultState;

    private State currentState;

    protected DFSM()
    {
        init();
    }

    /**
     * Initializes the state machine with the state and actions defined from the
     * argument.
     * 
     * @param stateTransitionMap
     * 
     * @param defaultState
     * 
     */
    private void init()
    {
        Map<String, Map<String, String>> stateTransitionMap = getInitData();
        String defaultState = getDefaultState();
        for (Entry<String, Map<String, String>> entry : stateTransitionMap.entrySet())
        {
            new State(entry.getKey(), entry.getValue());
        }

        this.defaultState = indexedState.get(defaultState);

        if (this.defaultState == null)
        {
            throw new IllegalArgumentException("Invalid default state specified");
        }

        this.setCurrentState(this.defaultState);
    }

    private void setCurrentState(State currentState)
    {
        this.currentState = currentState;
    }

    public String getCurrentState()
    {
        return this.currentState.getState();
    }

    /**
     * Makes the transition for the given action on the current state of the
     * machine. This results in state change.
     * 
     * @param action
     *            The action to be applied on the current state of the machine.
     * @return String representing the resulting state.
     */
    public String transition(String action)
    {
        State nextState = currentState.next(action);

        if (nextState == null)
        {
            throw new IllegalStateException(
                    MessageFormat.format("The action {0} is not available for the state {1}",
                            action, currentState.getState()));
        }

        currentState = indexedState.get(nextState.getState());

        return nextState.getState();
    }

    /**
     * Method that just checks what will be the next state. This itself doesn't
     * do any change for the state machine
     * 
     * @param currentState
     *            The current state.
     * @param action
     *            The action to be done on the given state.
     * @return The result state when the given action happens on the given
     *         state.
     */
    public String whatNext(String currentState, String action)
    {
        State currStateInst = indexedState.get(currentState);

        if (currStateInst == null)
        {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The given state {0} is not available in the given state machine",
                    currentState));
        }

        State nextState = currStateInst.next(action);

        return nextState != null ? nextState.getState() : null;
    }

    /**
     * Resets the {@link DFSM} to the default state.
     * 
     * @return The default state of the DFSM.
     */
    public String reset()
    {
        currentState = defaultState;
        return currentState.getState();
    }

    /**
     * Expected to return the transition map required to initialize the state
     * machine
     * 
     * @return Map containing the state transitions, where the keys contains the
     *         state and the values contains another map. The inner map
     *         represents the state transition with the keys as action and
     *         values as result state.
     */
    protected abstract Map<String, Map<String, String>> getInitData();

    /**
     * Expected to return the default state for the state machine.
     * 
     * @return The default state of the state machine. Cannot be null. And
     *         should be one among the states as per getInitData.
     */
    protected abstract String getDefaultState();

    /**
     * Class represents a state of the {@link StateMachine}
     * 
     * @author kannan.r
     *
     */
    public class State
    {

        private String state;

        private Map<Action, State> transition;

        public State(String name)
        {
            state = name;
            transition = new HashMap<>();
            State primaryState = indexedState.get(name);

            if (primaryState == null)
            {
                primaryState = this;
                indexedState.put(name, primaryState);
            }
        }

        /**
         * Initializes a state along with the transition mapping. The
         * <i>action</i> and the <i>result state</i> will be given as a string.
         * 
         * @param name
         *            The name of the state to instantiated
         * @param transitionMap
         *            The {@link Map} describing the action and the resultant
         *            state.
         */
        public State(String name, Map<String, String> transitionMap)
        {
            this(name);

            State primaryState = indexedState.get(name);

            for (Entry<String, String> entry : transitionMap.entrySet())
            {
                State aResultState = indexedState.get(entry.getValue());
                if (aResultState == null)
                {
                    aResultState = new State(entry.getValue());
                    indexedState.put(entry.getValue(), aResultState);
                }

                primaryState.addTransition(entry.getKey(), entry.getValue());
            }
        }

        /**
         * Adds a transition to the given state
         * 
         * @param action
         *            The action that triggers the transition
         * @param resultState
         *            The result state
         */
        protected void addTransition(String action, String resultState)
        {
            Action axn = new Action(action);
            State aResult = new State(resultState);
            State existingState = transition.get(axn);

            if (existingState == null || existingState.equals(aResult))
            {
                transition.put(axn, aResult);
            } else
            {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The {0} in {1} is already mapped to {2}, now cannot be changed to {3}",
                        action, state, existingState, resultState));
            }
        }

        /**
         * Moves to state machine to the next state by accepting the given
         * action.
         * 
         * @param action
         *            The action to be applied on the state machine
         * @return The result state
         */
        protected State next(String action)
        {
            return transition.get(new Action(action));
        }

        @Override
        public boolean equals(Object toCompare)
        {
            if (toCompare == null || !(toCompare instanceof State))
            {
                return false;
            }
            return state.equals(((State) toCompare).state);
        }

        @Override
        public int hashCode()
        {
            return state.hashCode();
        }

        public String getState()
        {
            return state;
        }

        public void setState(String state)
        {
            this.state = state;
        }

        @Override
        public String toString()
        {
            StringBuilder aStr = new StringBuilder();
            aStr.append("{");

            for (Entry<Action, State> entry : transition.entrySet())
            {
                aStr.append(entry.getKey().toString())
                        .append(':')
                        .append(entry.getValue().getState())
                        .append(',');
            }

            if (transition.size() > 0)
            {

                aStr.deleteCharAt(aStr.length() - 1);
            }
            aStr.append('}');

            return aStr.toString();
        }
    }

    /**
     * Represents an action that is applicable on a given state.
     * 
     * @author kannan.r
     *
     */
    public static class Action
    {
        private String action;

        public Action(String action)
        {
            this.action = action;
        }

        public String getAction()
        {
            return action;
        }

        @Override
        public boolean equals(Object toCompare)
        {
            if (toCompare == null || !(toCompare instanceof Action))
            {
                return false;
            }
            return action.equals(((Action) toCompare).action);
        }

        @Override
        public int hashCode()
        {
            return action.hashCode();
        }

        @Override
        public String toString()
        {
            return action;
        }
    }
}
