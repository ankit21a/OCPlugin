package org.opendaylight.oc.neutron;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.NetworkIpam;
import net.juniper.contrail.api.types.SubnetType;
import net.juniper.contrail.api.types.VirtualNetwork;
import net.juniper.contrail.api.types.VnSubnetsType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronNetwork;
import org.opendaylight.controller.networkconfig.neutron.NeutronSubnet;

/**
 * Test Class for Subnet.
 */
public class SubnetHandlerTest {

    SubnetHandler SubnetHandler;
    NeutronSubnet mockedNeutronSubnet = mock(NeutronSubnet.class);
    NeutronNetwork mockedNeutronNetwork = mock(NeutronNetwork.class);
    VirtualNetwork mockedVirtualNetwork = mock(VirtualNetwork.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    SubnetType mockedSubnetType = mock(SubnetType.class);
    NetworkIpam mockedNetworkIpam = mock(NetworkIpam.class);
    SubnetHandler subnetMock = mock(SubnetHandler.class);


    @Before
    public void beforeTest(){
        SubnetHandler = new SubnetHandler();
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedNeutronNetwork);
        assertNotNull(mockedVirtualNetwork);
    }

    @After
    public void afterTest(){
        SubnetHandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for Neutron Subnet  */

    public NeutronSubnet defaultSubnetObject(){
        NeutronSubnet subnet = new NeutronSubnet();
        subnet.setNetworkUUID("6b9570f2-17b1-4fc399ec-1b7f7778a29b");
        subnet.setSubnetUUID("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
        subnet.setCidr("10.0.0.1/24");
        subnet.setGatewayIP("10.0.0.0");
        return subnet;
    }

    /* Test method to check if neutron subnet is null */
    @Test
    public void testCanCreateSubnetNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, SubnetHandler.canCreateSubnet(null));
    }


    /*  Test method to check if Api server is available */
    @Test
    public void testcanCreateSubnetApiConnectorNull() {
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        assertEquals(HttpURLConnection.HTTP_UNAVAILABLE, SubnetHandler.canCreateSubnet(neutronSubnet));
    }


    /* Test method to check if virtual network is null */
    @Test
    public void testCanCreateSubnetVirtualNetworkNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class,neutronSubnet.getNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, SubnetHandler.canCreateSubnet(neutronSubnet));
    }


    /* Test method to check if subnet can be created with IpamRefs null */
    @Test
    public void testCanCreateSubnet() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class,neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(null);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, SubnetHandler.canCreateSubnet(neutronSubnet));
    }


    /* Test method to check if subnet creation returns Internal Server Error  */
    @Test
    public void testCanCreateSubnetException() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class,neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(null);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, SubnetHandler.canCreateSubnet(neutronSubnet));
    }


    /* Test method to check if subnet already exists  */

    @Test
    public void testCanCreateSubnetExists() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class,neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference <VnSubnetsType> ref=new ObjectReference<>();
        List<ObjectReference <VnSubnetsType>> ipamRefs = new ArrayList<ObjectReference <VnSubnetsType>>();
        List<VnSubnetsType.IpamSubnetType> subnets=new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp=new ArrayList<String>();
        for(int i=0;i<1;i++) {
            subnetType.setSubnet(type);
            subnetType.getSubnet().setIpPrefix("10.0.0.1");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp,vnSubnetType,"","");
            ipamRefs.add(ref);
            }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        assertTrue(subnetType.getSubnet().getIpPrefix().matches("10.0.0.1"));
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, SubnetHandler.canCreateSubnet(neutronSubnet));
        }


    /* Test method to check if subnet already exists, with null CIDR */
    @Test
    public void testCanCreateSubnetExistsWithCIDRNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = new NeutronSubnet();
        neutronSubnet.setNetworkUUID("6b9570f2-17b1-4fc399ec-1b7f7778a29b");
        neutronSubnet.setSubnetUUID("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
        when(mockedApiConnector.findById(VirtualNetwork.class,neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference <VnSubnetsType> ref=new ObjectReference<>();
        List<ObjectReference <VnSubnetsType>> ipamRefs = new ArrayList<ObjectReference <VnSubnetsType>>();
        List<VnSubnetsType.IpamSubnetType> subnets=new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp=new ArrayList<String>();
        for(int i=0;i<1;i++) {
            subnetType.setSubnet(type);
            subnetType.getSubnet().setIpPrefix("10.0.0.1");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp,vnSubnetType,"","");
            ipamRefs.add(ref);
            }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        assertTrue(subnetType.getSubnet().getIpPrefix().matches("10.0.0.1"));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, SubnetHandler.canCreateSubnet(neutronSubnet));
        }


    /* Test method to check ipPrefix  */
    @Test
    public void testGetIpPrefix() {
        SubnetHandler.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        String cidr = "10.0.0.1/24";
        String[] ipPrefix = cidr.split("/");
        assertArrayEquals(ipPrefix, SubnetHandler.getIpPrefix(neutronSubnet));
    }


    /* Test method to check if ipPrefix is null  */
    @Test(expected=NullPointerException.class)
    public void testGetIpPrefixNull() {
        SubnetHandler.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = new NeutronSubnet();
        neutronSubnet.setNetworkUUID("6b9570f2-17b1-4fc399ec-1b7f7778a29b");
        neutronSubnet.setSubnetUUID("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
        SubnetHandler.getIpPrefix(neutronSubnet);
    }


    /* Test method to check if ipPrefix is valid  */
    @Test(expected=IllegalArgumentException.class)
    public void testGetIpPrefixInvalid() {
        SubnetHandler.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = new NeutronSubnet();
        neutronSubnet.setNetworkUUID("6b9570f2-17b1-4fc399ec-1b7f7778a29b");
        neutronSubnet.setSubnetUUID("7b9570f2-17b1-4fc399ec-1b7f7778a29b");
        neutronSubnet.setCidr("10.0.0.1");
        SubnetHandler.getIpPrefix(neutronSubnet);
    }


    /* Test method to check if network is available  */
    @Test
    public void testGetNetwork() throws IOException {
        SubnetHandler.apiConnector = mockedApiConnector;
        NeutronSubnet neutronSubnet = defaultSubnetObject();
        when(mockedApiConnector.findById(VirtualNetwork.class,neutronSubnet.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        assertNotNull(SubnetHandler.getNetwork(neutronSubnet));
    }
}