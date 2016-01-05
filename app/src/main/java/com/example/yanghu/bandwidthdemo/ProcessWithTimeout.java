package com.example.yanghu.bandwidthdemo;

/**
 * Created by yanghu on 1/5/16.
 */
public class ProcessWithTimeout extends Thread
{
    private static String LOG_TAG;
    static {
        LOG_TAG = "PROCESSWITHTIMEOUT";
    }
    private Process m_process;
    private int m_exitCode = Integer.MIN_VALUE;
    private String m_tag;

    public ProcessWithTimeout(Process p_process, String tag)
    {
        m_tag = tag;
        m_process = p_process;
    }

    public int waitForProcess(int p_timeoutMilliseconds)
    {
        this.start();

        try
        {
            this.join(p_timeoutMilliseconds);
        }
        catch (InterruptedException e)
        {
            this.interrupt();
            System.out.println("process timeout" );
        }
        catch (Exception e){
            System.err.println("exception for waiting for prcocess "+e.toString() );
        }

        return m_exitCode;
    }

    @Override
    public void run()
    {
        try
        {
            m_exitCode = m_process.waitFor();
        }
        catch (InterruptedException ignore)
        {
            System.err.println("InterruptedException for process: "+m_tag);
            ignore.printStackTrace();
        }
        catch (Exception ex)
        {
            System.err.println("error wait for process: "+m_tag);
            ex.printStackTrace();
        }
    }
}