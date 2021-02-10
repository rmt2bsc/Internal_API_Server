package org.rmt2.handler.projecttracker.project;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import org.dto.Project2Dto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modules.ProjectTrackerApiConst;
import org.modules.admin.ProjectAdminApi;
import org.modules.admin.ProjectAdminApiException;
import org.modules.admin.ProjectAdminApiFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rmt2.api.handlers.admin.project.ProjectDeleteApiHandler;
import org.rmt2.handler.BaseMockSingleConsumerMDBTest;

import com.api.messaging.jms.JmsClientManager;
import com.api.util.RMT2File;



/**
 * Test the idenity and invocation of the project delete related JMS messages
 * for the Project Tracker API Message Handler.
 * 
 * @author appdev
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProjectDeleteApiHandler.class, JmsClientManager.class, ProjectAdminApiFactory.class })
public class ProjectDeleteJmsTest extends BaseMockSingleConsumerMDBTest {

    private static final String DESTINATION = "rmt2.queue.projecttracker";
    public static final int PROJECT_ID_NEW = 4444;
    private ProjectAdminApi mockApi;


    /**
     * 
     */
    public ProjectDeleteJmsTest() {
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
        this.mockApi = Mockito.mock(ProjectAdminApi.class);
        PowerMockito.mockStatic(ProjectAdminApiFactory.class);
        when(ProjectAdminApiFactory.createApi(eq(ProjectTrackerApiConst.APP_NAME))).thenReturn(this.mockApi);
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
    public void invokeHandelrSuccess_Delete() {
        String request = RMT2File.getFileContentsAsString("xml/projecttracker/admin/ProjectDeleteRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.deleteProject(isA(Project2Dto.class))).thenReturn(1);
        } catch (ProjectAdminApiException e) {
            e.printStackTrace();
            Assert.fail("Project update test case failed");
        }

        try {
            this.startTest();    
            Mockito.verify(this.mockApi).deleteProject(isA(Project2Dto.class));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
        
    }

}
