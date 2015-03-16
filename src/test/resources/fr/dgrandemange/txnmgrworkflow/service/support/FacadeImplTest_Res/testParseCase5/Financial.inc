	<txnmgr:group name="Financial">
		<txnmgr:participant class="fr.dgrandemange.demoapp.participant.CheckRequiredFields">
			<txnmgr:property name="fields" value="0,3,4,17,49,32,41,43,37,PAN,AMOUNT" />

			<txnmgr:property name="ok" value="Financial_Suite1" selectCriterion="checked" />
			<txnmgr:property name="nok" value="" selectCriterion="unchecked" />			
		</txnmgr:participant>
	</txnmgr:group>

	<txnmgr:group name="Financial_Suite1">
		<txnmgr:participant class="fr.dgrandemange.demoapp.participant.CheckCurrency">
			<txnmgr:property name="ok" value="Financial_Suite2" selectCriterion="checked" />
			<txnmgr:property name="nok" value="" selectCriterion="unchecked" />
		</txnmgr:participant>
	</txnmgr:group>


	<txnmgr:group name="Financial_Suite2">
		<txnmgr:participant class="fr.dgrandemange.demoapp.participant.FinancialProcessingSuite" />
	</txnmgr:group>