package modeal.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

public class MakeCalendarData {
	public static void main(String[] args) {
		List<DateVo> list = new ArrayList<DateVo>();
		DateVo vo;
		
		Calendar c1 = new GregorianCalendar();
		Calendar c2 = new GregorianCalendar();
		
		for (int i = 0; i < 1000; i++) {
			vo = new DateVo();
			vo.setDate(new SimpleDateFormat("yyyyMMdd").format(c1.getTime()));
			int iWeek = c1.get(Calendar.DAY_OF_WEEK);
			vo.setRemark(iWeek == 1 ? "�Ͽ���" : iWeek == 7 ? "�����" : "");
			list.add(vo);
			c1.add(Calendar.DATE, -1);
		}
		for (int i = 0; i < 1000; i++) {
			c2.add(Calendar.DATE, +1);
			vo = new DateVo();
			vo.setDate(new SimpleDateFormat("yyyyMMdd").format(c2.getTime()));
			int iWeek = c2.get(Calendar.DAY_OF_WEEK);
			vo.setRemark(iWeek == 1 ? "�Ͽ���" : iWeek == 7 ? "�����" : "");
			list.add(vo);
		}
		
		Comparator<DateVo> sort = new Comparator<DateVo>() {
			public int compare(DateVo o1, DateVo o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		};
		Collections.sort(list, sort);
		
		for (DateVo resultVo : list) {
			System.out.println("INSERT INTO CRM_CALENDAR (DATE, REMARK, REG_ID, REG_DTM) VALUES (\"" + resultVo.getDate() + "\", \"" + resultVo.getRemark() + "\", \"900000001\", NOW());");
		}
	}
}

class DateVo {
	private String date;
	private String remark;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}