package org.rmt2.handler.accounting.transaction.cashreceipts;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;

import org.dao.transaction.XactDao;
import org.dao.transaction.XactDaoFactory;
import org.dto.XactCustomCriteriaDto;
import org.dto.XactDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modules.transaction.XactApi;
import org.modules.transaction.XactApiException;
import org.modules.transaction.XactApiFactory;
import org.modules.transaction.receipts.CashReceiptApi;
import org.modules.transaction.receipts.CashReceiptApiException;
import org.modules.transaction.receipts.CashReceiptApiFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rmt2.AccountingMockData;
import org.rmt2.BaseMockMessageDrivenBeanTest;
import org.rmt2.api.handlers.transaction.receipts.CashReceiptsApiHandler;

import com.api.messaging.jms.JmsClientManager;
import com.api.util.RMT2File;

/**
 * Test the idenity and invocation of the Cash Receipts Transaction API Message
 * Handler.
 * 
 * @author appdev
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ JmsClientManager.class, XactApiFactory.class, CashReceiptsApiHandler.class, CashReceiptApiFactory.class })
public class CashReceiptsJmsTest extends BaseMockMessageDrivenBeanTest {

    private static final String DESTINATION = "Test-Accounting-Queue";
    private CashReceiptApi mockApi;

    public static final int NEW_XACT_ID = 1234567;

    /**
     * 
     */
    public CashReceiptsJmsTest() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see testcases.messaging.MessageToListenerToHandlerTest#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Setup Xact DAO mocks
        XactDaoFactory mockXactDaoFactory = Mockito.mock(XactDaoFactory.class);
        XactDao mockDao = Mockito.mock(XactDao.class);
        when(mockXactDaoFactory.createRmt2OrmXactDao(isA(String.class))).thenReturn(mockDao);

        // Setup Xact API mocks
        XactCustomCriteriaDto mockCustomCriteriaDto = XactApiFactory.createCustomCriteriaInstance();
        XactApi mockXactApi = Mockito.mock(XactApi.class);
        PowerMockito.mockStatic(XactApiFactory.class);
        PowerMockito.when(XactApiFactory.createDefaultXactApi()).thenReturn(mockXactApi);
        PowerMockito.when(XactApiFactory.createCustomCriteriaInstance()).thenReturn(mockCustomCriteriaDto);

        // Setup Cash Receipts API Mocks
        mockApi = Mockito.mock(CashReceiptApi.class);
        PowerMockito.mockStatic(CashReceiptApiFactory.class);
        PowerMockito.when(CashReceiptApiFactory.createApi()).thenReturn(this.mockApi);
        doNothing().when(this.mockApi).close();
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see testcases.messaging.MessageToListenerToHandlerTest#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        return;
    }

    @Test
    public void invokeHandlerSucceess_Fetch() {
        String request = RMT2File.getFileContentsAsString("xml/transaction/receipts/CashReceiptQueryRequest.xml");
        List<XactDto> mockListData = AccountingMockData.createMockCashReceiptTransactions();

        // Setup JMS Mocks
        this.setupMocks(DESTINATION, request);

        try {
            when(this.mockApi.getXact(isA(XactDto.class))).thenReturn(mockListData);
        } catch (XactApiException e) {
            Assert.fail("Unable to setup JMS mock stub for fetching a cash receipt transactions");
        }

        try {
            this.startTest();
            Mockito.verify(this.mockApi).getXact(isA(XactDto.class));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }

    @Test
    public void invokeHandlerSuccess_Create() {
        String request = RMT2File.getFileContentsAsString("xml/transaction/receipts/CashReceiptCreateRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.receivePayment(isA(XactDto.class), isA(Integer.class))).thenReturn(AccountingMockData.NEW_XACT_ID);
        } catch (CashReceiptApiException e) {
            Assert.fail("Unable to setup JMS mock stub for creating a cash receipt transactions");
        }

        try {
            this.startTest();
            Mockito.verify(this.mockApi).receivePayment(isA(XactDto.class), isA(Integer.class));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }

    @Test
    public void invokeHandlerError_Fetch_Incorrect_Trans_Code() {
        String request = RMT2File.getFileContentsAsString("xml/transaction/receipts/CashReceiptInvalidCodeQueryRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            this.startTest();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }

}
