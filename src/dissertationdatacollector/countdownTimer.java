/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dissertationdatacollector;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author joe yearsley
 */
public class countdownTimer extends Service{
    public Boolean cont = false;
    //Multi-task to take load off GUI thread
    protected synchronized Task<Void> createTask() {
        return new Task<Void>() {
         @Override protected Void call() throws Exception
        {
            
            long start = System.currentTimeMillis();
            long totalSeconds = 10;
            updateMessage("10");
            while(cont)
            {
                long tNow = System.currentTimeMillis();
                long elapsed = (tNow - start) / 1000;
                String sSec;
                if(elapsed < totalSeconds)
                {
                    
                    int baseElapsed = Integer.valueOf(String.valueOf(elapsed));
                    int baseTotal = Integer.valueOf(String.valueOf(totalSeconds));
                    int secTotal = baseTotal - baseElapsed;
                    int s1 = secTotal % 60;
                    sSec = String.valueOf(s1);
                    updateMessage(sSec);
                    if(s1 <= 0)
                    {
                        this.cancel();
                    }
                    if(isCancelled())
                    {
                        break;
                    }

                }
                else
                {
                    updateMessage("0");
                    cont = false;
                }
                try
                {
                   Thread.sleep(1000);
                }
                catch(InterruptedException interrupted)
                {
                    if(isCancelled())
                    {
                        updateMessage("Thread Cancelled");
                        break;
                    }

                }
            }
            updateMessage("10");
            return null;
        }
    };
};
}
