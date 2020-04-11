package com.j2eefast.system.job.controller;
import java.util.List;
import java.util.Map;

import com.j2eefast.common.core.controller.BaseController;
import com.j2eefast.common.core.enums.BusinessType;
import com.j2eefast.common.core.utils.ResponseData;
import com.j2eefast.common.core.controller.BaseController;
import com.j2eefast.framework.utils.CronUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.j2eefast.common.core.utils.PageUtil;
import com.j2eefast.common.core.utils.ResponseData;
import com.j2eefast.common.core.utils.ValidatorUtil;
import com.j2eefast.framework.annotation.RepeatSubmit;
import com.j2eefast.common.core.business.annotaion.BussinessLog;
import com.j2eefast.framework.quartz.entity.SysJobEntity;
import com.j2eefast.framework.quartz.service.SysJobService;

/**
 * 定时任务
 */
@Controller
@RequestMapping("/sys/schedule")
public class SysJobController extends BaseController {

	private String urlPrefix = "modules/sys/job";

	@Autowired
	private SysJobService sysJobService;

	/**
	 * 定时任务列表
	 */
	@RequestMapping("/list")
	@RequiresPermissions("sys:job:list")
	@ResponseBody
	public ResponseData list(@RequestParam Map<String, Object> params) {
		PageUtil page = sysJobService.findPage(params);
		return success(page);
	}

	@RequiresPermissions("sys:job:view")
	@GetMapping()
	public String job() {
		return urlPrefix + "/job";
	}

	@RequiresPermissions("sys:job:add")
	@GetMapping("/cron")
	public String cron() {
		return urlPrefix + "/cron";
	}


	@RequiresPermissions("sys:job:detail")
	@GetMapping("/detail/{jobId}")
	public String detail(@PathVariable("jobId") Long jobId, ModelMap mmap)
	{
		mmap.put("name", "job");
		mmap.put("job", sysJobService.getById(jobId));
		return urlPrefix + "/detail";
	}

	/**
	 * 保存定时任务
	 */
	@BussinessLog(title = "定时任务", businessType = BusinessType.INSERT)
	@RequestMapping("/add")
	@RequiresPermissions("sys:job:add")
	@ResponseBody
	@RepeatSubmit
	public ResponseData save(@Validated SysJobEntity sysJob) {

		ValidatorUtil.validateEntity(sysJob);

		sysJobService.add(sysJob);

		return success();
	}

	/**
	 * 修改调度
	 */
	@GetMapping("/edit/{jobId}")
	public String edit(@PathVariable("jobId") Long jobId, ModelMap mmap){
		mmap.put("job", sysJobService.getById(jobId));
		return urlPrefix + "/edit";
	}

	/**
	 * 修改定时任务
	 */
	@BussinessLog(title = "定时任务", businessType = BusinessType.UPDATE)
	@RequestMapping("/edit")
	@RequiresPermissions("sys:job:edit")
	@ResponseBody
	@RepeatSubmit
	public ResponseData update(SysJobEntity sysJob) {

		ValidatorUtil.validateEntity(sysJob);

		sysJobService.updateSysJob(sysJob);

		return success();
	}

	/**
	 * 删除定时任务
	 */
	@BussinessLog(title = "定时任务", businessType = BusinessType.DELETE)
	@RequestMapping(value = "/del", method = RequestMethod.POST)
	@RequiresPermissions("sys:job:del")
	@ResponseBody
	@RepeatSubmit
	public ResponseData del(Long[] ids) {

		return sysJobService.deleteBatchByIds(ids)?success():error("删除失败!");

	}

	/**
	 * 立即执行任务
	 */
	@BussinessLog(title = "定时任务", businessType = BusinessType.UPDATE)
	@RequestMapping(value = "/run", method = RequestMethod.POST)
	@RequiresPermissions("sys:job:run")
	@ResponseBody
	@RepeatSubmit
	public ResponseData run(Long[] jobIds) {
		sysJobService.run(jobIds);
		return success();
	}

	/**
	 * 暂停定时任务
	 */
	@BussinessLog(title = "定时任务", businessType = BusinessType.UPDATE)
	@RequestMapping(value = "/pause", method = RequestMethod.POST)
	@RequiresPermissions("sys:job:changeStatus")
	@ResponseBody
	@RepeatSubmit
	public ResponseData pause(Long[] jobIds) {
		sysJobService.pause(jobIds);
		return success();
	}


	/**
	 * 新增调度
	 */
	@GetMapping("/add")
	public String add()
	{
		return urlPrefix + "/add";
	}

	/**
	 * 恢复定时任务
	 */
	@BussinessLog(title = "定时任务", businessType = BusinessType.UPDATE)
	@RequestMapping(value = "/resume", method = RequestMethod.POST)
	@RequiresPermissions("sys:job:changeStatus")
	@ResponseBody
	@RepeatSubmit
	public ResponseData resume(Long[] jobIds) {
		sysJobService.resume(jobIds);
		return success();
	}


	/**
	 * 校验cron表达式是否有效
	 */
	@PostMapping("/checkCronExpressionIsValid")
	@ResponseBody
	public ResponseData checkCronExpressionIsValid(SysJobEntity sysJob)
	{
		if(sysJobService.checkCronExpressionIsValid(sysJob.getCronExpression())){
			return success();
		}
		return error("无效");
	}

	/**
	 * 查询cron表达式近10次的执行时间
	 */
	@GetMapping("/checkCronExpression")
	@ResponseBody
	public ResponseData checkCronExpression(@RequestParam(value = "CronExpression", required = false) String CronExpression){
		if(sysJobService.checkCronExpressionIsValid(CronExpression)){
			List<String> dateList =  CronUtils.getRecentTriggerTime(CronExpression);
			return success(dateList);
		}else {
			return error("表达式无效");
		}
	}

}