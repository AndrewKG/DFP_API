package com.dfp.apiextractor;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201505.StatementBuilder;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by keanguan on 7/2/15.
 */
public class DFPAPIAccess {

    private static DFPAPIAccess singleton = null;
    private static final int SUGGESTED_PAGE_LIMIT = StatementBuilder.SUGGESTED_PAGE_LIMIT;
    //need to update when updated to new versions
    private static final String PACKAGE_PATH = "com.google.api.ads.dfp.axis.v201505.";

    DfpServices dfpServices = null;
    DfpSession session = null;
    //static final String apiPropertiesFilePath = "/Users/keanguan/svnrepos/dfp_axis/ads.properties";
   // static final String apiPropertiesFilePath = "/Users/bait/Desktop/ads.properties";
    static final String apiPropertiesFilePath =ExtractionInstruction.apiPropertiesFilePath;
    protected DFPAPIAccess() throws Exception {
// Generate a refreshable OAuth2 credential.ß
    	System.out.println("apiPropertiesFilePath:"+apiPropertiesFilePath);
        Credential oAuth2Credential = new OfflineCredentials.Builder()
                .forApi(OfflineCredentials.Api.DFP)
                .fromFile(apiPropertiesFilePath)
                .build()
                .generateCredential();

        // Construct a DfpSession.
        session = new DfpSession.Builder()
                .fromFile(apiPropertiesFilePath)
                .withOAuth2Credential(oAuth2Credential)
                .build();
        dfpServices = new DfpServices();
    }

    public static DFPAPIAccess getInstance() throws Exception {
        if (singleton == null) {
            singleton = new DFPAPIAccess();
        }
        return singleton;
    }

    private Object getServiceObject(String objectServiceInterfacePath) throws Exception {
        // Get the ObjectService.
        if(StringUtils.isBlank(objectServiceInterfacePath)) {
            throw new Exception("Service Class name missing");
        }
        objectServiceInterfacePath = PACKAGE_PATH + objectServiceInterfacePath;
        return dfpServices.get(session, Class.forName(objectServiceInterfacePath));
    }

    private Object getFirst(String serviceInterfacePath,  String serviceMethodName, StatementBuilder statementBuilder) throws Exception {
        Object serviceObject = getServiceObject(serviceInterfacePath);

        // Get by statement.
        Object pageObject = invokeMethod(serviceObject, serviceMethodName, statementBuilder.toStatement());

        Object[] results = (Object[]) PropertyUtils.getProperty(pageObject, "results");
        return results == null || results.length == 0 ? null : results[0];
    }

    private List getAll(String serviceInterfacePath, String objectClassPath, String serviceMethodName, StatementBuilder statementBuilder) throws Exception {
        Object serviceObject = getServiceObject(serviceInterfacePath);
        List<Object> ret = new ArrayList<>();

        int totalResultSetSize = 0;

        do {
            Object pageObject = invokeMethod(serviceObject, serviceMethodName, statementBuilder.toStatement());

            Object[] results = (Object[]) PropertyUtils.getProperty(pageObject, "results");
            ret.addAll(Arrays.asList(results));

            statementBuilder.increaseOffsetBy(SUGGESTED_PAGE_LIMIT);
            totalResultSetSize = (int) PropertyUtils.getProperty(pageObject, "totalResultSetSize");

        }while (statementBuilder.getOffset() < totalResultSetSize);
        return ret;
    }
    public Object getObjectById(String id, String serviceInterfacePath, String serviceMethodName) throws Exception {
        StatementBuilder statementBuilder = new StatementBuilder()
                .where("id = " + id)
                .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);
        return getFirst(serviceInterfacePath, serviceMethodName, statementBuilder);
    }
    public List<Object> getObjectsByIds(List<Long> ids, String objectClassPath, String serviceInterfacePath, String serviceMethodName) throws Exception {
        String whereClause = "";
        for (long id : ids) {
            whereClause += "id = " + id + " OR ";
        }

        //remove the last " OR "
        whereClause = whereClause.substring(0, whereClause.length()-4);

        StatementBuilder statementBuilder = new StatementBuilder()
                .where(whereClause)
                .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);
        return getAll(serviceInterfacePath, objectClassPath, serviceMethodName, statementBuilder);
    }

    public List<Object> getObjectsByParentIds(String parentFieldName, List<Long> parentIds, String objectClassPath, String serviceInterfacePath, String serviceMethodName) throws Exception {
        String whereClause = "";
        if(parentIds.size()<1) {
            return new ArrayList<>(0);
        }

        for (long id : parentIds) {
            whereClause += parentFieldName + " = " + id + " OR ";
        }

        //remove the last " OR "
        whereClause = whereClause.substring(0, whereClause.length()-4);

        StatementBuilder statementBuilder = new StatementBuilder()
                .where(whereClause)
                .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT);
        return getAll(serviceInterfacePath, objectClassPath, serviceMethodName, statementBuilder);
    }

    public static Object invokeMethod (Object parentObject, String methodName, Object arg) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> parentClass = parentObject.getClass();
        Method method  = parentClass.getMethod(methodName, arg.getClass());
        return method.invoke(parentObject, arg);
    }
}
