package fr.viveris.s1pdgs.scaler.k8s;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.K8sUnknownResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.PodResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStopException;
import fr.viveris.s1pdgs.scaler.k8s.services.NodeService;
import fr.viveris.s1pdgs.scaler.k8s.services.PodService;
import fr.viveris.s1pdgs.scaler.k8s.services.WrapperService;

public class K8SAdministrationTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private WrapperProperties wrapperProperties;
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private NodeService nodeService;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private PodService podService;
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) 
	private WrapperService wrapperService;
	
	@InjectMocks
	private K8SAdministration k8SAdministration;
	
	@Before
	public void init() throws PodResourceException, K8sUnknownResourceException, WrapperStopException {
		MockitoAnnotations.initMocks(this);
		when(wrapperProperties.getLabelWrapperConfig().getLabel()).thenReturn("wrapperconfig");
		when(wrapperProperties.getLabelWrapperConfig().getValue()).thenReturn("l1");
		when(wrapperProperties.getLabelWrapperStateUnused().getLabel()).thenReturn("wrapperstate");
		when(wrapperProperties.getLabelWrapperStateUnused().getValue()).thenReturn("unused");
		when(wrapperProperties.getLabelWrapperStateUsed().getLabel()).thenReturn("wrapperstate");
		when(wrapperProperties.getLabelWrapperStateUsed().getValue()).thenReturn("used");
		when(wrapperProperties.getLabelWrapperApp().getLabel()).thenReturn("app");
		when(wrapperProperties.getLabelWrapperApp().getValue()).thenReturn("l1-wrapper");
		when(wrapperProperties.getPodTemplateFile()).thenReturn("config/template_l1_wrapper_pod.yml");
		doNothing().when(nodeService).editLabelToNode(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		doNothing().when(podService).createPodFromTemplate(Mockito.anyString(), Mockito.anyInt());
	}
	
	@Test
	public void testSetWrapperNodeUsable() {
		this.k8SAdministration.setWrapperNodeUsable("nodeName");
	}
	
	@Test
	public void testSetWrapperNodeUnusable() {
		this.k8SAdministration.setWrapperNodeUnusable("nodeName");
	}
	
	@Test
	public void testLaunchWrapperPodsPool() throws PodResourceException, K8sUnknownResourceException {
		this.k8SAdministration.launchWrapperPodsPool(1, new AtomicInteger(0));
	}
	
	@Test
	public void testStopWrapperPods() throws WrapperStopException {
		List<String> stopPod = new ArrayList<>();
		stopPod.add("ipPod");
		doNothing().when(wrapperService).stopWrapper(Mockito.anyString());
		this.k8SAdministration.stopWrapperPods(stopPod);
	}
	
	@Test(expected = WrapperStopException.class)
	public void testStopWrapperPodsWrapperStopException() throws WrapperStopException {
		List<String> stopPod = new ArrayList<>();
		stopPod.add("ipPod");
		doThrow(new WrapperStopException("ip", String.format("Queryfailed with code %s", "404"))).when(wrapperService).stopWrapper(Mockito.anyString());
		this.k8SAdministration.stopWrapperPods(stopPod);
	}
	
	@Test
	public void testDeleteTerminatedWrapperPods() throws PodResourceException, K8sUnknownResourceException {
		List<PodDesc> pods = new ArrayList<>();
		pods.add(new PodDesc("name1-suffixe"));
		pods.add(new PodDesc("name2-suffixe"));
		pods.add(new PodDesc("name3-suffixe"));
		doReturn(pods).when(podService).getPodsWithLabelAndStatusPhase(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		doReturn(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE).when(podService).deletePodFromTemplate(Mockito.anyString(), Mockito.anyString());
		
		List<String> expectedResult = new ArrayList<>();
		expectedResult.add("name1-suffixe");
		expectedResult.add("KO-name2-suffixe");
		expectedResult.add("name3-suffixe");
		
		List<String> result = this.k8SAdministration.deleteTerminatedWrapperPods();
		assertEquals("Result is different from expected result", expectedResult, result);
		
	}
	
	@Test(expected = PodResourceException.class)
	public void testDeleteTerminatedWrapperPodsPodResourceException() throws PodResourceException, K8sUnknownResourceException {
		List<PodDesc> pods = new ArrayList<>();
		pods.add(new PodDesc("name1-suffixe"));
		pods.add(new PodDesc("name2-suffixe"));
		pods.add(new PodDesc("name3-suffixe"));
		doReturn(pods).when(podService).getPodsWithLabelAndStatusPhase(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		doThrow(new PodResourceException("No resources loaded from file")).when(podService).deletePodFromTemplate(Mockito.anyString(), Mockito.anyString());
		
		this.k8SAdministration.deleteTerminatedWrapperPods();
		
	}
	
	@Test(expected = K8sUnknownResourceException.class)
	public void testDeleteTerminatedWrapperPodsK8sUnknownResourceException() throws PodResourceException, K8sUnknownResourceException {
		List<PodDesc> pods = new ArrayList<>();
		pods.add(new PodDesc("name1-suffixe"));
		pods.add(new PodDesc("name2-suffixe"));
		pods.add(new PodDesc("name3-suffixe"));
		doReturn(pods).when(podService).getPodsWithLabelAndStatusPhase(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		doThrow(new K8sUnknownResourceException("Unknown kind")).when(podService).deletePodFromTemplate(Mockito.anyString(), Mockito.anyString());
		
		this.k8SAdministration.deleteTerminatedWrapperPods();
		
	}
	

}
