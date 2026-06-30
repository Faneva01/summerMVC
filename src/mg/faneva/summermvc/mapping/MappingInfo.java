package mg.faneva.summermvc.mapping;

public class MappingInfo {
    private String className;
    private String methodName;

    public MappingInfo(){
    }

    public MappingInfo(String className, String methodName){
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }
    public String getMethodName() {
        return methodName;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
