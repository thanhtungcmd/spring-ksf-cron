package com.ksf.job.contract;

import com.ksf.job.contract.authen.Auth;
import com.ksf.job.contract.authen.Auto;
import com.ksf.job.contract.order.BondOrderContract;
import com.ksf.job.contract.order.InvestOrderContract;
import com.ksf.job.contract.order.InvestPlusOrderContract;
import com.ksf.job.contract.order.InvestPlusRenderContract;

public class Main {

    public static void main(String[] args) {
        /*InvestOrderContract investOrderContract = new InvestOrderContract();
        investOrderContract.execAll();*/

        InvestPlusOrderContract investPlusOrderContract = new InvestPlusOrderContract();
        investPlusOrderContract.execAll();

        /*BondOrderContract bondOrderContract = new BondOrderContract();
        bondOrderContract.exec();*/

        /*InvestPlusRenderContract investPlusRenderContract = new InvestPlusRenderContract();
        investPlusRenderContract.exec();*/
    }

}