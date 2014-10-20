package com.musala.atmosphere.server;


public class PoolManagerEmulatorCreationTest {
    // private static final int POOL_MANAGER_RMI_PORT = 1235;
    //
    // private static final long EMULATOR_CREATION_TIMEOUT = ServerPropertiesLoader.getEmulatorCreationTimeout();
    //
    // private static final String AGENT_ID = "mockagent";
    //
    // private static final String EMULATOR_NAME = "emulator_007";
    //
    // private static final String EMULATOR_SERIAL = "007";
    //
    // private static final String EMULATOR_RMI_ID = "mockagent_007";
    //
    // private static final String AGENT_ALLOCATOR_FIELD_NAME = "agentAllocator";
    //
    // private static final long WAIT_FOR_EMULATOR_EXISTS_TIMEOUT = EMULATOR_CREATION_TIMEOUT * 5 / 30;
    //
    // private static final long WAIT_FOR_DEVICE_EXISTS_TIMEOUT = EMULATOR_CREATION_TIMEOUT * 4 / 5;
    //
    // private static ServerManager serverManager;
    //
    // private static PoolManager poolManager;
    //
    // private static Field agentAllocatorField;
    //
    // private static Registry mockedRegistry;
    //
    // private static IWrapDevice mockedDevice;
    //
    // private static DeviceParameters sampleDeviceParameters;
    //
    // /**
    // * Used to register a device to the {@link PoolManager} during the execution of a void method.
    // */
    // private static class AddDeviceAnswer implements Answer<Void> {
    // private String deviceSerialNumber;
    //
    // private Registry agentRegistry;
    //
    // private IAgentManager agentManager;
    //
    // public AddDeviceAnswer(String deviceSerialNumber, Registry agentRegistry, IAgentManager agentManager) {
    // super();
    // this.deviceSerialNumber = deviceSerialNumber;
    // this.agentRegistry = agentRegistry;
    // this.agentManager = agentManager;
    // }
    //
    // @Override
    // public Void answer(InvocationOnMock invocation) throws Throwable {
    // poolManager.addDevice(deviceSerialNumber, agentRegistry, agentManager);
    // return null;
    // }
    // }
    //
    // /**
    // * Reflects the {@link AgentAllocator} field of the {@link PoolManager} and replaces it with a mocked object that
    // * returns the given {@link IAgentManager} when {@link AgentAllocator#getAgent()} is called.
    // *
    // */
    // private static void changeAgentManagerMock(IAgentManager mockedAgentManager) throws Exception {
    // AgentAllocator mockedAgentAllocator = mock(AgentAllocator.class);
    // doReturn(mockedAgentManager).when(mockedAgentAllocator).getAgent();
    //
    // if (agentAllocatorField == null) {
    // Class<?> poolManagerClass = poolManager.getClass();
    // agentAllocatorField = poolManagerClass.getDeclaredField(AGENT_ALLOCATOR_FIELD_NAME);
    // agentAllocatorField.setAccessible(true);
    // }
    //
    // agentAllocatorField.set(poolManager, mockedAgentAllocator);
    // }
    //
    // @BeforeClass
    // public static void setUp() throws Exception {
    // serverManager = new ServerManager(POOL_MANAGER_RMI_PORT);
    // poolManager = PoolManager.getInstance();
    //
    // mockedDevice = mock(IWrapDevice.class);
    // DeviceInformation mockDeviceInformation = new DeviceInformation();
    // mockDeviceInformation.setSerialNumber(EMULATOR_SERIAL);
    // doReturn(mockDeviceInformation).when(mockedDevice).route(RoutingAction.GET_DEVICE_INFORMATION);
    //
    // mockedRegistry = mock(Registry.class);
    // doReturn(mockedDevice).when(mockedRegistry).lookup(EMULATOR_SERIAL);
    //
    // sampleDeviceParameters = new DeviceParameters();
    // sampleDeviceParameters.setApiLevel(19);
    // }
    //
    // @AfterClass
    // public static void tearDown() throws Exception {
    // serverManager.close();
    // }
    //
    // @Test
    // public void testCreateEmulator() throws Exception {
    // IAgentManager mockedAgentManager = mock(IAgentManager.class);
    // doReturn(AGENT_ID).when(mockedAgentManager).getAgentId();
    // doReturn(EMULATOR_NAME).when(mockedAgentManager).createAndStartEmulator(any(EmulatorParameters.class));
    // doReturn(EMULATOR_SERIAL).when(mockedAgentManager).getSerialNumberOfEmulator(anyString());
    //
    // AddDeviceAnswer addDeviceAnswer = new AddDeviceAnswer(EMULATOR_SERIAL, mockedRegistry, mockedAgentManager);
    // doAnswer(addDeviceAnswer).when(mockedAgentManager).waitForDeviceExists(EMULATOR_SERIAL,
    // WAIT_FOR_DEVICE_EXISTS_TIMEOUT);
    //
    // changeAgentManagerMock(mockedAgentManager);
    //
    // String allocatedRmiId = poolManager.allocateDevice(sampleDeviceParameters).getProxyRmiId();
    // assertEquals(EMULATOR_RMI_ID, allocatedRmiId);
    //
    // verify(mockedAgentManager, times(1)).createAndStartEmulator(any(EmulatorParameters.class));
    // verify(mockedAgentManager, times(1)).waitForEmulatorExists(EMULATOR_NAME, WAIT_FOR_EMULATOR_EXISTS_TIMEOUT);
    // verify(mockedAgentManager, times(1)).getSerialNumberOfEmulator(EMULATOR_NAME);
    // verify(mockedAgentManager, times(1)).waitForDeviceExists(EMULATOR_SERIAL, WAIT_FOR_DEVICE_EXISTS_TIMEOUT);
    //
    // poolManager.removeDevice(EMULATOR_SERIAL, AGENT_ID, mockedRegistry);
    // }
    //
    // @Test(expected = EmulatorCreationFailedException.class)
    // public void testCreateAndStartEmulatorFailure() throws Exception {
    // IAgentManager mockedAgentManager = mock(IAgentManager.class);
    // doReturn(AGENT_ID).when(mockedAgentManager).getAgentId();
    // doThrow(IOException.class).when(mockedAgentManager).createAndStartEmulator(any(EmulatorParameters.class));
    //
    // changeAgentManagerMock(mockedAgentManager);
    //
    // poolManager.allocateDevice(sampleDeviceParameters);
    // }
    //
    // @Test(expected = EmulatorCreationFailedException.class)
    // public void testWaitForEmulatorExistsFailure() throws Exception {
    // IAgentManager mockedAgentManager = mock(IAgentManager.class);
    // doReturn(AGENT_ID).when(mockedAgentManager).getAgentId();
    // doReturn(EMULATOR_NAME).when(mockedAgentManager).createAndStartEmulator(any(EmulatorParameters.class));
    // doThrow(TimeoutReachedException.class).when(mockedAgentManager).waitForEmulatorExists(anyString(), anyLong());
    //
    // changeAgentManagerMock(mockedAgentManager);
    //
    // poolManager.allocateDevice(sampleDeviceParameters);
    // }
    //
    // @Test(expected = EmulatorCreationFailedException.class)
    // public void testGetSerialNumberFailure() throws Exception {
    // IAgentManager mockedAgentManager = mock(IAgentManager.class);
    // doReturn(AGENT_ID).when(mockedAgentManager).getAgentId();
    // doReturn(EMULATOR_NAME).when(mockedAgentManager).createAndStartEmulator(any(EmulatorParameters.class));
    // doThrow(DeviceNotFoundException.class).when(mockedAgentManager).getSerialNumberOfEmulator(anyString());
    //
    // changeAgentManagerMock(mockedAgentManager);
    //
    // poolManager.allocateDevice(sampleDeviceParameters);
    // }
    //
    // @Test(expected = EmulatorCreationFailedException.class)
    // public void testWaitForDeviceExistsFailure() throws Exception {
    // IAgentManager mockedAgentManager = mock(IAgentManager.class);
    // doReturn(AGENT_ID).when(mockedAgentManager).getAgentId();
    // doReturn(EMULATOR_NAME).when(mockedAgentManager).createAndStartEmulator(any(EmulatorParameters.class));
    // doReturn(EMULATOR_SERIAL).when(mockedAgentManager).getSerialNumberOfEmulator(anyString());
    // doThrow(TimeoutReachedException.class).when(mockedAgentManager).waitForDeviceExists(anyString(), anyLong());
    //
    // changeAgentManagerMock(mockedAgentManager);
    //
    // poolManager.allocateDevice(sampleDeviceParameters);
    // }
    //
    // @Test(expected = EmulatorCreationFailedException.class)
    // public void testServerPublishingFailure() throws Exception {
    // IAgentManager mockedAgentManager = mock(IAgentManager.class);
    // doReturn(AGENT_ID).when(mockedAgentManager).getAgentId();
    // doReturn(EMULATOR_NAME).when(mockedAgentManager).createAndStartEmulator(any(EmulatorParameters.class));
    // doReturn(EMULATOR_SERIAL).when(mockedAgentManager).getSerialNumberOfEmulator(anyString());
    //
    // changeAgentManagerMock(mockedAgentManager);
    //
    // poolManager.allocateDevice(sampleDeviceParameters);
    // }
}
