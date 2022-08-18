package com.ksf.job.contract;

import com.ksf.job.contract.order.BondOrderContract;
import com.ksf.job.contract.order.InvestOrderContract;
import com.ksf.job.contract.order.InvestPlusOrderContract;

public class Main {

    public static void main(String[] args) {
        InvestOrderContract investOrderContract = new InvestOrderContract();
        investOrderContract.execAll();

        InvestPlusOrderContract investPlusOrderContract = new InvestPlusOrderContract();
        investPlusOrderContract.execAll();

        BondOrderContract bondOrderContract = new BondOrderContract();
        bondOrderContract.execAll();
    }

}
