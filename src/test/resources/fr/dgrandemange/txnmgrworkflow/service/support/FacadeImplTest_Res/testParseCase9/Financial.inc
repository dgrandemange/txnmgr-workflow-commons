	<group name="Financial">
		<participant class="fr.dgrandemange.demoapp.participant.CheckRequiredFields">
			<property name="fields" value="0,3,4,17,49,32,41,43,37,PAN,AMOUNT" />

			<property name="ok" value="Financial_Suite1" selectCriterion="checked" />
			<property name="nok" value="" selectCriterion="unchecked" />			
		</participant>
	</group>

	<group name="Financial_Suite1">
		<participant class="fr.dgrandemange.demoapp.participant.CheckCurrency">
			<property name="ok" value="Financial_Suite2" selectCriterion="checked" />
			<property name="nok" value="" selectCriterion="unchecked" />
		</participant>
	</group>


	<group name="Financial_Suite2">
		<participant class="fr.dgrandemange.demoapp.participant.FinancialProcessingSuite" />
	</group>