package com.syn.iorder;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;

import syn.pos.data.dao.ShopProperty;
import syn.pos.data.model.ShopData;
import syn.pos.data.model.ShopData.CourseInfo;

public class Course {

	private Context mContext;
	private OnCourseClickedListener mCourseClickedListener;
	private int mCourseId;
	private List<ShopData.CourseInfo> mCourseLst;
	private ShopProperty mShopProperty;
	private int mLastBtnId = -1;
	
	private HorizontalScrollView mHorScrollView;
	
	public Course(Context c, int currCourseId, HorizontalScrollView hView, OnCourseClickedListener listener){
		mContext = c;
		mShopProperty = new ShopProperty(c, null);
		mCourseLst = mShopProperty.listAllCourse();
		
		mCourseId = currCourseId;
		mHorScrollView = hView;
		mCourseClickedListener = listener;
	}
	
	public void createCourseView(){
		LayoutInflater inflater = (LayoutInflater) 
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final LinearLayout content = (LinearLayout) mHorScrollView.findViewById(R.id.content);
		
		ShopData.CourseInfo courseInfo = new ShopData.CourseInfo();
		courseInfo.setCourseID(0);
		courseInfo.setCourseName("All");
		courseInfo.setCourseShortName("All");
		mCourseLst.add(0, courseInfo);
				
		for(final ShopData.CourseInfo course : mCourseLst){
			LinearLayout.LayoutParams params = 
					new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
							LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
			params.setMargins(2, 0, 2, 0);
			final Button btnCourse = (Button) inflater.inflate(R.layout.course_button, null);
			btnCourse.setId(course.getCourseID());
			btnCourse.setLayoutParams(params);
			btnCourse.setText(course.getCourseName());
			
			if(mCourseId == course.getCourseID()){
				btnCourse.setActivated(true);
				mLastBtnId = course.getCourseID();
			}else{
				btnCourse.setActivated(false);
			}
			
			btnCourse.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if (course.getCourseID() == mLastBtnId) {
						if(btnCourse.isActivated())
							btnCourse.setActivated(false);
						else
							btnCourse.setActivated(true);
					} else {
						btnCourse.setActivated(true);
						try {
							((Button) content.findViewById(mLastBtnId))
									.setActivated(false);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					mLastBtnId = course.getCourseID();
					
					mCourseClickedListener.onClick(course.getCourseID(),
							course.getCourseName(), course.getCourseShortName());
				}
				
			});
			content.addView(btnCourse);
		}
	}
	
	public static interface OnCourseClickedListener{
		void onClick(int courseId, String courseName, String courseShortName);
	}
}
