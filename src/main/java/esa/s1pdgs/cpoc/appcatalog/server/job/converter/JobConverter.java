package esa.s1pdgs.cpoc.appcatalog.server.job.converter;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobFileDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Converter for job objects
 * 
 * @author Viveris Technologies
 */
public class JobConverter<T> {

    /**
     * Product category corresponding to the generic type
     */
    private final ProductCategory category;

    /**
     * Constructor
     * 
     * @param category
     */
    public JobConverter(final ProductCategory category) {
        this.category = category;
    }

    /**
     * Convert a job from db to dto
     * 
     * @param jobDb
     * @return
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    @SuppressWarnings("unchecked")
    public AppDataJobDto<T> convertJobFromDbToDto(final AppDataJob jobDb)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        AppDataJobDto<T> jobDto = new AppDataJobDto<>();
        jobDto.setIdentifier(jobDb.getIdentifier());
        jobDto.setLevel(jobDb.getLevel());
        jobDto.setProduct(convertJobProductFromDbToDto(jobDb.getProduct()));
        jobDto.setPod(jobDb.getPod());
        jobDto.setState(convertJobStateFromDbToDto(jobDb.getState()));
        jobDto.setCreationDate(jobDb.getCreationDate());
        jobDto.setLastUpdateDate(jobDb.getLastUpdateDate());
        for (Object jobMsgDb : jobDb.getMessages()) {
            jobDto.getMessages().add((GenericMessageDto<T>) jobMsgDb);
        }
        for (AppDataJobGeneration jobGenDb : jobDb.getGenerations()) {
            jobDto.getGenerations()
                    .add(convertJobGenerationFromDbToDto(jobGenDb));
        }
        return jobDto;
    }

    /**
     * Convert a job from dto to db
     * 
     * @param jobDto
     * @return
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    public AppDataJob convertJobFromDtoToDb(final AppDataJobDto<T> jobDto)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        AppDataJob jobDb = new AppDataJob();
        jobDb.setIdentifier(jobDto.getIdentifier());
        jobDb.setLevel(jobDto.getLevel());
        jobDb.setProduct(convertJobProductFromDtoToDb(jobDto.getProduct()));
        jobDb.setPod(jobDto.getPod());
        jobDb.setState(convertJobStateFromDtoToDb(jobDto.getState()));
        jobDb.setCreationDate(jobDto.getCreationDate());
        jobDb.setLastUpdateDate(jobDto.getLastUpdateDate());
        jobDb.setCategory(category);
        for (GenericMessageDto<T> jobMsgDb : jobDto.getMessages()) {
            jobDb.getMessages().add(jobMsgDb);
        }
        for (AppDataJobGenerationDto jobGenDto : jobDto.getGenerations()) {
            jobDb.getGenerations()
                    .add(convertJobGenerationFromDtoToDb(jobGenDto));
        }
        return jobDb;
    }

    /**
     * @param jobGenDb
     * @return
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    public AppDataJobGenerationDto convertJobGenerationFromDbToDto(
            AppDataJobGeneration jobGenDb)
            throws AppCatalogJobGenerationInvalidStateException {
        AppDataJobGenerationDto jobGenDto = new AppDataJobGenerationDto();
        jobGenDto.setCreationDate(jobGenDb.getCreationDate());
        jobGenDto.setLastUpdateDate(jobGenDb.getLastUpdateDate());
        jobGenDto.setTaskTable(jobGenDb.getTaskTable());
        jobGenDto.setState(
                convertJobGenerationStateFromDbToDto(jobGenDb.getState()));
        jobGenDto.setNbErrors(jobGenDb.getNbErrors());
        return jobGenDto;
    }

    /**
     * @param jobGenDb
     * @return
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    public AppDataJobGeneration convertJobGenerationFromDtoToDb(
            AppDataJobGenerationDto jobGenDto)
            throws AppCatalogJobGenerationInvalidStateException {
        AppDataJobGeneration jobGenDb = new AppDataJobGeneration();
        jobGenDb.setCreationDate(jobGenDto.getCreationDate());
        jobGenDb.setLastUpdateDate(jobGenDto.getLastUpdateDate());
        jobGenDb.setTaskTable(jobGenDto.getTaskTable());
        jobGenDb.setState(
                convertJobGenerationStateFromDtoToDb(jobGenDto.getState()));
        jobGenDb.setNbErrors(jobGenDto.getNbErrors());
        return jobGenDb;
    }

    /**
     * Convert a job product from db to dto
     * 
     * @param jobProductDb
     * @return
     */
    public AppDataJobProductDto convertJobProductFromDbToDto(
            final AppDataJobProduct jobProductDb) {
        AppDataJobProductDto jobProductDto = null;
        if (jobProductDb != null) {
            jobProductDto = new AppDataJobProductDto();
            jobProductDto.setProductName(jobProductDb.getProductName());
            jobProductDto.setMissionId(jobProductDb.getMissionId());
            jobProductDto.setSatelliteId(jobProductDb.getSatelliteId());
            jobProductDto.setSessionId(jobProductDb.getSessionId());
            jobProductDto.setStartTime(jobProductDb.getStartTime());
            jobProductDto.setStopTime(jobProductDb.getStopTime());
            jobProductDto.setAcquisition(jobProductDb.getAcquisition());
            jobProductDto.setDataTakeId(jobProductDb.getDataTakeId());
            jobProductDto.setInsConfId(jobProductDb.getInsConfId());
            jobProductDto.setNumberSlice(jobProductDb.getNumberSlice());
            jobProductDto.setProductType(jobProductDb.getProductType());
            jobProductDto.setSegmentStartDate(jobProductDb.getSegmentStartDate());
            jobProductDto.setSegmentStopDate(jobProductDb.getSegmentStopDate());
            jobProductDto.setTotalNbOfSlice(jobProductDb.getTotalNbOfSlice());
            jobProductDto.setProcessMode(jobProductDb.getProcessMode());
            if (!CollectionUtils.isEmpty(jobProductDb.getRaws1())) {
                for (AppDataJobFile fileDb : jobProductDb.getRaws1()) {
                    jobProductDto.getRaws1()
                            .add(convertJobFileFromDbToDto(fileDb));
                }
            }
            if (!CollectionUtils.isEmpty(jobProductDb.getRaws2())) {
                for (AppDataJobFile fileDb : jobProductDb.getRaws2()) {
                    jobProductDto.getRaws2()
                            .add(convertJobFileFromDbToDto(fileDb));
                }
            }
        }
        return jobProductDto;
    }

    /**
     * Convert a job product from dto to db
     * 
     * @param jobProductDto
     * @return
     */
    public AppDataJobProduct convertJobProductFromDtoToDb(
            final AppDataJobProductDto jobProductDto) {
        AppDataJobProduct jobProductDb = null;
        if (jobProductDto != null) {
            jobProductDb = new AppDataJobProduct();
            jobProductDb.setProductName(jobProductDto.getProductName());
            jobProductDb.setMissionId(jobProductDto.getMissionId());
            jobProductDb.setSatelliteId(jobProductDto.getSatelliteId());
            jobProductDb.setSessionId(jobProductDto.getSessionId());
            jobProductDb.setStartTime(jobProductDto.getStartTime());
            jobProductDb.setStopTime(jobProductDto.getStopTime());
            jobProductDb.setAcquisition(jobProductDto.getAcquisition());
            jobProductDb.setAcquisition(jobProductDto.getAcquisition());
            jobProductDb.setDataTakeId(jobProductDto.getDataTakeId());
            jobProductDb.setInsConfId(jobProductDto.getInsConfId());
            jobProductDb.setNumberSlice(jobProductDto.getNumberSlice());
            jobProductDb.setProductType(jobProductDto.getProductType());
            jobProductDb.setSegmentStartDate(jobProductDto.getSegmentStartDate());
            jobProductDb.setSegmentStopDate(jobProductDto.getSegmentStopDate());
            jobProductDb.setTotalNbOfSlice(jobProductDto.getTotalNbOfSlice());
            jobProductDb.setProcessMode(jobProductDto.getProcessMode());
            if (!CollectionUtils.isEmpty(jobProductDto.getRaws1())) {
                for (AppDataJobFileDto fileDto : jobProductDto.getRaws1()) {
                    jobProductDb.getRaws1()
                            .add(convertJobFileFromDtoToDb(fileDto));
                }
            }
            if (!CollectionUtils.isEmpty(jobProductDto.getRaws2())) {
                for (AppDataJobFileDto fileDto : jobProductDto.getRaws2()) {
                    jobProductDb.getRaws2()
                            .add(convertJobFileFromDtoToDb(fileDto));
                }
            }
        }
        return jobProductDb;
    }

    /**
     * 
     * @param fileDb
     * @return
     */
    public AppDataJobFileDto convertJobFileFromDbToDto(AppDataJobFile fileDb) {
        return new AppDataJobFileDto(fileDb.getFilename(), fileDb.getKeyObs());
    }

    /**
     * 
     * @param fileDto
     * @return
     */
    public AppDataJobFile convertJobFileFromDtoToDb(AppDataJobFileDto fileDto) {
        return new AppDataJobFile(fileDto.getFilename(), fileDto.getKeyObs());
    }

    /**
     * Convert job state from db to dto
     * 
     * @param jobStateDb
     * @return
     * @throws AppCatalogJobInvalidStateException
     */
    public AppDataJobDtoState convertJobStateFromDbToDto(
            final AppDataJobState jobStateDb)
            throws AppCatalogJobInvalidStateException {
        AppDataJobDtoState jobStateDto = AppDataJobDtoState.WAITING;
        switch (jobStateDb) {
            case WAITING:
                jobStateDto = AppDataJobDtoState.WAITING;
                break;
            case DISPATCHING:
                jobStateDto = AppDataJobDtoState.DISPATCHING;
                break;
            case GENERATING:
                jobStateDto = AppDataJobDtoState.GENERATING;
                break;
            case TERMINATED:
                jobStateDto = AppDataJobDtoState.TERMINATED;
                break;
            default:
                throw new AppCatalogJobInvalidStateException(jobStateDb.name(),
                        "DB");
        }
        return jobStateDto;
    }

    /**
     * Convert job state from dto to db
     * 
     * @param jobStateDb
     * @return
     * @throws AppCatalogJobInvalidStateException
     */
    public AppDataJobState convertJobStateFromDtoToDb(
            final AppDataJobDtoState jobStateDto)
            throws AppCatalogJobInvalidStateException {
        AppDataJobState jobStateDb = AppDataJobState.WAITING;
        switch (jobStateDto) {
            case WAITING:
                jobStateDb = AppDataJobState.WAITING;
                break;
            case DISPATCHING:
                jobStateDb = AppDataJobState.DISPATCHING;
                break;
            case GENERATING:
                jobStateDb = AppDataJobState.GENERATING;
                break;
            case TERMINATED:
                jobStateDb = AppDataJobState.TERMINATED;
                break;
            default:
                throw new AppCatalogJobInvalidStateException(jobStateDto.name(),
                        "DTO");
        }
        return jobStateDb;
    }

    /**
     * Convert job generation state from db to dto
     * 
     * @param jobGenStateDb
     * @return
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    public AppDataJobGenerationDtoState convertJobGenerationStateFromDbToDto(
            final AppDataJobGenerationState jobGenStateDb)
            throws AppCatalogJobGenerationInvalidStateException {
        AppDataJobGenerationDtoState jobGenStateDto =
                AppDataJobGenerationDtoState.INITIAL;
        switch (jobGenStateDb) {
            case INITIAL:
                jobGenStateDto = AppDataJobGenerationDtoState.INITIAL;
                break;
            case PRIMARY_CHECK:
                jobGenStateDto = AppDataJobGenerationDtoState.PRIMARY_CHECK;
                break;
            case READY:
                jobGenStateDto = AppDataJobGenerationDtoState.READY;
                break;
            case SENT:
                jobGenStateDto = AppDataJobGenerationDtoState.SENT;
                break;
            default:
                throw new AppCatalogJobGenerationInvalidStateException(
                        jobGenStateDb.name(), "DB");
        }
        return jobGenStateDto;
    }

    /**
     * Convert job generation state from db to dto
     * 
     * @param jobGenStateDb
     * @return
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    public AppDataJobGenerationState convertJobGenerationStateFromDtoToDb(
            final AppDataJobGenerationDtoState jobGenStateDto)
            throws AppCatalogJobGenerationInvalidStateException {
        AppDataJobGenerationState jobGenStateDb =
                AppDataJobGenerationState.INITIAL;
        switch (jobGenStateDto) {
            case INITIAL:
                jobGenStateDb = AppDataJobGenerationState.INITIAL;
                break;
            case PRIMARY_CHECK:
                jobGenStateDb = AppDataJobGenerationState.PRIMARY_CHECK;
                break;
            case READY:
                jobGenStateDb = AppDataJobGenerationState.READY;
                break;
            case SENT:
                jobGenStateDb = AppDataJobGenerationState.SENT;
                break;
            default:
                throw new AppCatalogJobGenerationInvalidStateException(
                        jobGenStateDto.name(), "DTO");
        }
        return jobGenStateDb;
    }
}
