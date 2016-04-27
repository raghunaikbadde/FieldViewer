package com.jobviwer.request.object;

public class Data
{
    private String entity;

    private String action;

    private Payload payload;

    public String getEntity ()
    {
        return entity;
    }

    public void setEntity (String entity)
    {
        this.entity = entity;
    }

    public String getAction ()
    {
        return action;
    }

    public void setAction (String action)
    {
        this.action = action;
    }

    public Payload getPayload ()
    {
        return payload;
    }

    public void setPayload (Payload payload)
    {
        this.payload = payload;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [entity = "+entity+", action = "+action+", payload = "+payload+"]";
    }
}
