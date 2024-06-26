/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.appcatalog;

import java.util.Date;
import java.util.Objects;

/**
 * Object used for persisting data around job generation per task table
 * 
 * @author Viveris Technologies
 */
public class AppDataJobGeneration {
    /**
     * Date of the creation of the job generation
     */
    private Date creationDate;

    /**
     * Date of the last modification done on the job generation
     */
    private Date lastUpdateDate;

    /**
     * Name of the task table (= filename)
     */
    private String taskTable;

    /**
     * State of the job generation
     */
    private AppDataJobGenerationState state;
    
    /**
     * Previous state of the job generation
     */
    private AppDataJobGenerationState previousState;

    /**
     * Number of consecutive errors
     */
    private int nbErrors;
    
    public AppDataJobGeneration() {
        super();
        this.state = AppDataJobGenerationState.INITIAL;
        this.previousState = AppDataJobGenerationState.INITIAL;
        this.creationDate = new Date();
        this.nbErrors = 0;
    }
    
    public AppDataJobGeneration(final AppDataJobGeneration clone) {
		this.creationDate = clone.creationDate;
		this.lastUpdateDate = clone.lastUpdateDate;
		this.taskTable = clone.taskTable;
		this.state = clone.state;
		this.previousState = clone.previousState;
		this.nbErrors = clone.nbErrors;
	}

	/**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastUpdateDate
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @param lastUpdateDate
     *            the lastUpdateDate to set
     */
    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * @return the taskTable
     */
    public String getTaskTable() {
        return taskTable;
    }

    /**
     * @param taskTable
     *            the taskTable to set
     */
    public void setTaskTable(final String taskTable) {
        this.taskTable = taskTable;
    }

    /**
     * @return the state
     */
    public AppDataJobGenerationState getState() {
        return state;
    }
    
    /**
     * @param state
     *            the state to set
     */
    public void setState(final AppDataJobGenerationState state) {
        this.state = state;
    }
    
    /**
     * @return the previous state
     */
    public AppDataJobGenerationState getPreviousState() {
        return previousState;
    }
    
    /**
     * @param previousState
     *            the previous state to set
     */
    public void setPreviousState(final AppDataJobGenerationState previousState) {
        this.previousState = previousState;
    }

    /**
     * @return the nbErrors
     */
    public int getNbErrors() {
        return nbErrors;
    }

    /**
     * @param nbErrors
     *            the nbErrors to set
     */
    public void setNbErrors(final int nbErrors) {
        this.nbErrors = nbErrors;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{creationDate: %s, lastUpdateDate: %s, taskTable: %s, state: %s, nbErrors: %s, prevState: %s}",
                creationDate, lastUpdateDate, taskTable, state, nbErrors, previousState);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(creationDate, lastUpdateDate, taskTable, state,
                nbErrors, previousState);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            final AppDataJobGeneration other = (AppDataJobGeneration) obj;
            ret = Objects.equals(creationDate, other.creationDate)
                    && Objects.equals(lastUpdateDate, other.lastUpdateDate)
                    && Objects.equals(taskTable, other.taskTable)
                    && Objects.equals(state, other.state)
                    && Objects.equals(previousState, other.previousState)
                    && nbErrors == other.nbErrors;
        }
        return ret;
    }

}
