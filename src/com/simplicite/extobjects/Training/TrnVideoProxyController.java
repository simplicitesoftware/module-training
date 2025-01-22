package com.simplicite.extobjects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.tools.*;
import javax.servlet.http.HttpServletResponse;
import com.simplicite.webapp.tools.ServletParameters;

/**
 * External object TrnVideoProxyController
 */
public class TrnVideoProxyController extends com.simplicite.util.ExternalObject {
	private static final long serialVersionUID = 1L;
	// Note: instead of this basic external object, a specialized subclass should be used

	/**
	 * Display method
	 * @param params Request parameters
	 */
	@Override
	public Object display(Parameters params) { 
		Grant g =  Grant.getSystemAdmin();
		String objName = params.getParameter("object");
		String id = params.getParameter("row_id");
		String field = params.getParameter("field");
		ObjectDB obj = g.getTmpObject(objName);
		synchronized(obj.getLock()){
			obj.resetFilters();
			obj.select(id);
			DocumentDB doc = obj.getField(field).getDocument();
			try{
				HttpServletResponse response = ((ServletParameters)params).getResponse();
				response.setContentType(doc.getMIME());
				response.setHeader("Content-Disposition", "inline; filename=\""+doc.getName()+"\"");
				response.setHeader("Accept-Ranges", "bytes");
				response.setContentLengthLong(doc.getSize());
				FileTool.writeFile(doc.getInputStream(), response.getOutputStream());
				return "";
			}catch(Exception e){
				AppLog.error(e,g);
				return "see log!";
			}
			
		}
	}
}