

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor {
	/*
	 * ------------
	 * Data members
	 * ------------
	 */

	//defines the state of philosophers
	private enum State {hungry, eating, talking, thinking}

	// holds a state for each philosopher
	private State[] state;

	int numOfPhilosophers;

	private boolean[] chopsticks ;

	private boolean aPhilosopherIsTalking = false;
	int philosophersWaitingToTalk = 0;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers) {
		// TODO: set appropriate number of chopsticks based on the # of philosophers
		this.numOfPhilosophers = piNumberOfPhilosophers;

		state = new State[numOfPhilosophers];
		chopsticks = new boolean[numOfPhilosophers];

		for (int i = 0; i < numOfPhilosophers; i++) {
			state[i] = State.thinking;
			chopsticks[i] = false;
		}

	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID) {

		int i = piTID - 1;

		// Indicate that I want to take chopsticks.
		System.out.format("Philosopher %d is hungry\n", i + 1);
		state[i] = State.hungry;

		// Pick up chopsticks if both neighbors are not eating.
		if ((state[(i - 1 + numOfPhilosophers) % numOfPhilosophers] != State.eating) &&
				(state[(i + 1) % numOfPhilosophers] != State.eating)) {
			System.out.format("Philosopher %d picks up left chopstick\n", i + 1);
			System.out.format("Philosopher %d picks up right chopstick\n", i + 1);

			state[i] = State.eating;
			// chopstick is taken
			chopsticks[i] = true;

		}
		else { // wait if at least one neighbor is eating
			while (state[i] != State.eating) {
				System.out.format("Chopsticks taken, philosopher %d needs to wait\n", i + 1);
				try {
					wait();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				//then update the state after both chopsticks are free
				state[i] = State.eating;

			}
		}

	}


	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		int i = piTID-1 ;

			System.out.format("Philosopher %d puts down right chopstick\n", i+1);
			System.out.format("Philosopher %d puts down left chopstick\n", i+1);

			state[i] = State.thinking;

			// Tell the left neighbor about the possibility to eat.
			int left = (i - 1 + numOfPhilosophers)%numOfPhilosophers;
			int left2 = (i - 2 + numOfPhilosophers)%numOfPhilosophers;
			if( (state[left] == State.hungry) &&
					(state[left2] != State.eating) ){
				chopsticks[left] = false; // put down left chopstick
			}
			// Tell the right neighbor about the possibility to eat
			if( (state[(i+1)%numOfPhilosophers] == State.hungry) &&
					(state[(i+2)%numOfPhilosophers] != State.eating) ){
				chopsticks[(i+1)%numOfPhilosophers]= false; // put down right chopstick
			}

		// the philosopher is finished putting down their left and right chopsticks and notifies others
		notifyAll();

	}

	/**
	 * Only one philosopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk()
	{
		try
		{
			// counter to see how many philosophers are in queue to talk
			philosophersWaitingToTalk++;
			while(aPhilosopherIsTalking) // if at least one is talking, wait
			{
				wait();
			}
			philosophersWaitingToTalk--; // once the philosopher has started talking, kick him out of the queue...
			aPhilosopherIsTalking = true; // ...and let him talk

		}
		catch (InterruptedException e)
		{
			System.err.println("Monitor.requestTalk():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}

	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk() {
		//A philosopher is no longer talking. Notify one waiting philosopher
		//that they can start talking.

		aPhilosopherIsTalking = false;
		if(philosophersWaitingToTalk > 0)
			notifyAll();
	}
}