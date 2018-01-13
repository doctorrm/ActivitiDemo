package org.activiti.spring;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.runtime.ProcessInstance;

public class BpmDefinitionTest extends BaseTestCase {
	@Autowired
	private RepositoryService repositoryService;

	private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	/**
	 * 部署流程。
	 * 通过定义好的流程图文件部署流程，一次只能部署一个流程。涉及act_re_deployment，act_re_procdef，act_ge_bytearray三张表
	 */
	public Deployment deploy() {
		Deployment deployment = repositoryService.createDeployment().name("SickLeave")
				.addClasspathResource("diagrams/MyProcess.bpmn").deploy();
		return deployment;
	}

	/**
	 * 启动流程 涉及多张表
	 * 
	 * @param instanceByKey
	 * @return
	 */
	public ProcessInstance startInstanceByKey(String instanceByKey) {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(instanceByKey);
		return instance;
	}

	/**
	 * 通过assignee来查看任务 涉及数据表：act_ru_task
	 * 
	 * @param assignee
	 * @return
	 */
	public List<Task> findTaskByAssignee(String assignee) {
		TaskService taskService = processEngine.getTaskService();
		List<Task> taskList = taskService.createTaskQuery().taskAssignee(assignee).list();
		return taskList;
	}

	/**
	 * 流程审批（执行各个任务节点）
	 * 
	 * @param taskid
	 *            ：就是任务id，是在数据表中的ID_列（如10008）中，不是流程图中的id（如usertask1）
	 * @param map
	 *            ：对应的两条if判断线中的main config 中的condition中的条件，map的键为‘day’
	 */
	public void completeTask(String taskid, Map<String, Object> map) {
		TaskService taskService = processEngine.getTaskService();
		taskService.complete(taskid, map);
	}

	/**
	 * 执行整个流程
	 * 
	 * @param dayNum：流程判断条件分支
	 */
	public void completeProcess(Integer dayNum) {
		// 查看请假任务
		List<Task> taskList1 = findTaskByAssignee("apply");// 本例只有一个task
		Task task1 = taskList1.get(0);
		System.out.println("员工请假任务为：" + task1 + "，task_id为：" + task1.getId() + "。此句输出后该任务将被删除");
		// 执行请假任务节点,执行完成后将自动删掉act_ru_task数据表中的待办task，并添加下一个待办task(经理task或者老板task)
		System.out.println("请假中。。。。。。");
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("day", dayNum);
		completeTask(task1.getId(), map1);
		System.out.println("请假完成并成功提交。");
		System.out.println("-----------------------等上级睡醒zzzzZZ-------------------------------");
		
		if (dayNum <= 3) {// 天数<=3,流向经理审批
			System.out.print("请假天数为" + dayNum + "天，不大于3天，由经理来审批。");
			// 查看经理审批任务
			List<Task> taskList2 = findTaskByAssignee("pm");
			Task task2 = taskList2.get(0);
			System.out.println("经理审批任务为：" + task2 + "，task_id为：" + task2.getId() + "。此句输出后该任务将被删除");
			// 经理审批，此任务执行完后数据表将重新变为空
			System.out.println("经理审批中。。。。。。");
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put("day", dayNum);
			completeTask(task2.getId(), map2);
			System.out.println("经理审批完成！");
		} else {// 天数>3,流向老板审批
			System.out.print("请假天数为" + dayNum + "天，大于3天，由老板亲自审批。");
			// 查看老板审批任务
			List<Task> taskList2 = findTaskByAssignee("boss");
			Task task2 = taskList2.get(0);
			System.out.println("老板审批任务为：" + task2 + "，task_id为：" + task2.getId()+ "。此句输出后该任务将被删除");
			// 老板审批，此任务执行完后数据表将重新变为空
			System.out.println("老板审批中。。。。。。");
			Map<String, Object> map2 = new HashMap<String, Object>();
			map2.put("day", dayNum);
			completeTask(task2.getId(), map2);
			System.out.println("老板审批完成！");
		}
		System.out.println("流程执行完毕，有关数据表任务为空");
	}

	@Test
	public void testProcess() throws IOException, ParseException {
		Deployment deployment = deploy();
		ProcessInstance instance = startInstanceByKey("leaveProcess");
		completeProcess(6);
	}

}