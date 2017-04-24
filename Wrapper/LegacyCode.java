package Wrapper;


/**
 *
 * @author Samsaini
 */
public class LegacyCode {
    //simple implementation for LegacyCode
    //providing the service 
    String getDivisors(Object num){
        String answer = null;
        if(num instanceof String){ 
            int n;
            try{
                n = Integer.parseInt((String) num);
            } catch(Exception e) {
                return e.getMessage();
            }
            answer = "Given number " + n + " is divisible by";
            for(int i=1;i<=n;i++)
            {
                if(n%i==0)
                {
                answer += " " + i;
                }
            }
        }else{
            answer = "Given input is not a Number(isnan)";
        }
        return answer;
    }
}
