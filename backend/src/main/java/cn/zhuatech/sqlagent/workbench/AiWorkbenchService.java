/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent.workbench;
import org.springframework.stereotype.Service;
import java.math.*;
import java.util.*;
import static cn.zhuatech.sqlagent.workbench.WorkbenchInput.*;
/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class AiWorkbenchService {
    /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
    public Map<String,Object> execute(Map<String,Object> r){
        validate(r);

        keys(r,"sql","bindings");String sql=text(r,"sql",32000);var bindings=object(r.get("bindings"));if(bindings.size()>100)throw new IllegalArgumentException("参数最多100个");for(var e:bindings.entrySet())if(!e.getKey().matches("[A-Za-z_][A-Za-z0-9_]{0,63}")||e.getValue() instanceof Map||e.getValue() instanceof List)throw new IllegalArgumentException("仅支持命名标量参数");
        Set<String> names=new TreeSet<>();int mode=0,block=0;
        for(int i=0;i<sql.length();i++){char c=sql.charAt(i),next=i+1<sql.length()?sql.charAt(i+1):'\0';
            if(mode==1||mode==2){char quote=mode==1?'\'':'"';if(c=='\\')throw new IllegalArgumentException("不支持反斜杠转义方言");if(c==quote){if(next==quote)i++;else mode=0;}continue;}
            if(mode==3){if(c=='\n'||c=='\r')mode=0;continue;}if(mode==4){if(c=='/'&&next=='*'){block++;i++;}else if(c=='*'&&next=='/'){if(--block==0)mode=0;i++;}continue;}
            if(c=='\''){mode=1;continue;}if(c=='"'){mode=2;continue;}if(c=='-'&&next=='-'){mode=3;i++;continue;}if(c=='/'&&next=='*'){mode=4;block=1;i++;continue;}
            if(c=='$'||c==96||c=='?')throw new IllegalArgumentException("仅支持标准引号与命名参数");
            if(c==':'&&next==':'){i++;continue;}if(c==':'&&(Character.isLetter(next)||next=='_')){int end=i+2;while(end<sql.length()&&(Character.isLetterOrDigit(sql.charAt(end))||sql.charAt(end)=='_'))end++;String name=sql.substring(i+1,end);if(!name.matches("[A-Za-z_][A-Za-z0-9_]{0,63}"))throw new IllegalArgumentException("参数名格式不正确");names.add(name);i=end-1;}
        }
        if(mode==1||mode==2||mode==4)throw new IllegalArgumentException("SQL引号或注释未闭合");
        var missing=names.stream().filter(x->!bindings.containsKey(x)).toList();var unused=bindings.keySet().stream().filter(x->!names.contains(x)).sorted().toList();
        return Map.of("valid",missing.isEmpty()&&unused.isEmpty(),"parameters",List.copyOf(names),"missingBindings",missing,"unusedBindings",unused,"executesSql",false,"securityBoundary","BINDING_LINT_ONLY");

    }
}
