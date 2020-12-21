package com.simplicite.commons.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Shared code TrnTools
 */
public class TrnTools implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	public static String toSnake(String str){
		return SyntaxTool.forceCase(StringUtils.stripAccents(str), SyntaxTool.SNAKE, true);
	}
}
