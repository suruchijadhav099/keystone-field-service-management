package com.zidio.keystone;

import com.zidio.keystone.domain.Role;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.WorkOrderStatus;
import com.zidio.keystone.domain.WorkOrderStatusHistory;
import com.zidio.keystone.repository.WorkOrderRepository;
import com.zidio.keystone.repository.WorkOrderStatusHistoryRepository;
import com.zidio.keystone.service.WorkOrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KeystoneApplicationTests {

	private WorkOrderRepository workOrderRepository;
	private WorkOrderStatusHistoryRepository historyRepository;
	private WorkOrderService workOrderService;

	@BeforeEach
	void setUp() {

		workOrderRepository =
				mock(WorkOrderRepository.class);

		historyRepository =
				mock(WorkOrderStatusHistoryRepository.class);

		workOrderService =
				new WorkOrderService(
						workOrderRepository,
						historyRepository
				);
	}

	// =========================================================
	// TEST 1: NEW -> ASSIGNED
	// =========================================================

	@Test
	void newWorkOrderCanBeAssigned() {

		WorkOrder workOrder = new WorkOrder();
		workOrder.setStatus(WorkOrderStatus.NEW);

		User technician = new User();
		technician.setId(2L);
		technician.setRole(Role.TECHNICIAN);

		User manager = new User();
		manager.setId(1L);
		manager.setRole(Role.MANAGER);

		when(workOrderRepository.findById(1L))
				.thenReturn(Optional.of(workOrder));

		when(workOrderRepository.save(any(WorkOrder.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		WorkOrder result =
				workOrderService.assignTechnician(
						1L,
						technician,
						manager
				);

		assertEquals(
				WorkOrderStatus.ASSIGNED,
				result.getStatus()
		);

		assertEquals(
				technician,
				result.getAssignedTo()
		);

		verify(historyRepository, times(1))
				.save(any(WorkOrderStatusHistory.class));
	}

	// =========================================================
	// TEST 2: INVALID TRANSITION
	// =========================================================

	@Test
	void invalidStatusTransitionShouldFail() {

		WorkOrder workOrder = new WorkOrder();
		workOrder.setStatus(WorkOrderStatus.NEW);

		User manager = new User();
		manager.setId(1L);
		manager.setRole(Role.MANAGER);

		when(workOrderRepository.findById(1L))
				.thenReturn(Optional.of(workOrder));

		assertThrows(
				RuntimeException.class,
				() -> workOrderService.updateStatus(
						1L,
						WorkOrderStatus.COMPLETED,
						manager
				)
		);

		verify(workOrderRepository, never())
				.save(any(WorkOrder.class));
	}

	// =========================================================
	// TEST 3: CLOSED WORK ORDER CANNOT CHANGE
	// =========================================================

	@Test
	void closedWorkOrderCannotBeChanged() {

		WorkOrder workOrder = new WorkOrder();
		workOrder.setStatus(WorkOrderStatus.CLOSED);

		User manager = new User();
		manager.setId(1L);
		manager.setRole(Role.MANAGER);

		when(workOrderRepository.findById(1L))
				.thenReturn(Optional.of(workOrder));

		assertThrows(
				RuntimeException.class,
				() -> workOrderService.updateStatus(
						1L,
						WorkOrderStatus.IN_PROGRESS,
						manager
				)
		);

		verify(workOrderRepository, never())
				.save(any(WorkOrder.class));
	}

	// =========================================================
	// TEST 4: TECHNICIAN CAN UPDATE ONLY ASSIGNED WORK ORDER
	// =========================================================

	@Test
	void technicianCannotUpdateAnotherTechniciansWorkOrder() {

		WorkOrder workOrder = new WorkOrder();
		workOrder.setStatus(WorkOrderStatus.ASSIGNED);

		User assignedTechnician = new User();
		assignedTechnician.setId(2L);
		assignedTechnician.setRole(Role.TECHNICIAN);

		User anotherTechnician = new User();
		anotherTechnician.setId(3L);
		anotherTechnician.setRole(Role.TECHNICIAN);

		workOrder.setAssignedTo(assignedTechnician);

		when(workOrderRepository.findById(1L))
				.thenReturn(Optional.of(workOrder));

		assertThrows(
				RuntimeException.class,
				() -> workOrderService.updateStatus(
						1L,
						WorkOrderStatus.IN_PROGRESS,
						anotherTechnician
				)
		);

		verify(workOrderRepository, never())
				.save(any(WorkOrder.class));
	}
}