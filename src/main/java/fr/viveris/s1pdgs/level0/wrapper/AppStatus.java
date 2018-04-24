package fr.viveris.s1pdgs.level0.wrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppStatus {

	private final WrapperStatus status;
	
	private boolean shallBeStopped;
	
	@Value("${status.max-error-counter}")
	private int maxErrorCounter;

	public AppStatus() {
		this.status = new WrapperStatus();
		shallBeStopped = false;
	}

	/**
	 * @return the status
	 */
	public synchronized WrapperStatus getStatus() {
		return status;
	}

	public synchronized void setWaiting() {
		this.status.setWaiting();
	}

	public synchronized void setProcessing() {
		this.status.setProcessing();
	}

	public synchronized void setStopping() {
		if (!this.status.isProcessing()) {
			this.setShallBeStopped(true);
		}
		this.status.setStopping();
	}

	public synchronized void setError() {
		this.status.setError(maxErrorCounter);
	}

	/**
	 * @return the shallBeStopped
	 */
	public synchronized boolean isShallBeStopped() {
		return shallBeStopped;
	}

	/**
	 * @param shallBeStopped the shallBeStopped to set
	 */
	public synchronized void setShallBeStopped(boolean shallBeStopped) {
		this.shallBeStopped = shallBeStopped;
	}

	public class WrapperStatus {

		private AppState state;

		private long dateLastChangeMs;

		private int errorCounter;

		public WrapperStatus() {
			this.state = AppState.WAITING;
			errorCounter = 0;
			dateLastChangeMs = System.currentTimeMillis();
		}

		/**
		 * @return the status
		 */
		public AppState getState() {
			return state;
		}

		/**
		 * @return the timeSinceLastChange
		 */
		public long getDateLastChangeMs() {
			return dateLastChangeMs;
		}

		/**
		 * @return the errorCounter
		 */
		public int getErrorCounter() {
			return errorCounter;
		}

		public void setWaiting() {
			if (!isStopping() && !isFatalError()) {
				state = AppState.WAITING;
				dateLastChangeMs = System.currentTimeMillis();
			}
		}

		public void setProcessing() {
			if (!isStopping() && !isFatalError()) {
				state = AppState.PROCESSING;
				dateLastChangeMs = System.currentTimeMillis();
				errorCounter = 0;
			}
		}

		public void setStopping() {
			state = AppState.STOPPING;
			dateLastChangeMs = System.currentTimeMillis();
			errorCounter = 0;
		}

		public void setError(int maxErrorCounter) {
			if (!isStopping()) {
				state = AppState.ERROR;
				dateLastChangeMs = System.currentTimeMillis();
				errorCounter++;
				if (errorCounter >= maxErrorCounter) {
					setFatalError();
				}
			}
		}

		public void setFatalError() {
			state = AppState.FATALERROR;
			dateLastChangeMs = System.currentTimeMillis();
		}

		public boolean isWaiting() {
			return state == AppState.WAITING;
		}

		public boolean isProcessing() {
			return state == AppState.PROCESSING;
		}

		public boolean isStopping() {
			return state == AppState.STOPPING;
		}

		public boolean isError() {
			return state == AppState.ERROR;
		}

		public boolean isFatalError() {
			return state == AppState.FATALERROR;
		}

	}
	
	@Scheduled(fixedDelayString="${status.delete-fixed-delay-ms}")
	public void forceStopping() {
		if (this.isShallBeStopped()) {
			System.exit(0);
		}
	}
}
