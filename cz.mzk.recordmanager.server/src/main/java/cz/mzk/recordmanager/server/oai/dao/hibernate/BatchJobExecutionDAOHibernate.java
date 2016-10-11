package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.Library;
import cz.mzk.recordmanager.server.model.batch.BatchJobExecution;
import cz.mzk.recordmanager.server.oai.dao.BatchJobExecutionDAO;

import java.math.BigInteger;

/**
 * Created by sergey on 10/11/16.
 */
public class BatchJobExecutionDAOHibernate extends
        AbstractDomainDAOHibernate<BigInteger, BatchJobExecution> implements BatchJobExecutionDAO {

}
