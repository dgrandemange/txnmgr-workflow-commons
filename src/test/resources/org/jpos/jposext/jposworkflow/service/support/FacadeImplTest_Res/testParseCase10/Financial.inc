			<group name="Financial">
				<participant
					class="com.mbs.springframework.txnmgr.participant.CheckRequiredFields">
					<property name="fields"
						value="0,3,4,17,49,32,41,43,37,PAN,AMOUNT" />

					<property name="txOk" value="Financial_Suite1"
						selectCriterion="checked" />
					<property name="txNok" value=""
						selectCriterion="unchecked" />
				</participant>
			</group>

			<group name="Financial_Suite1">
				<participant
					class="com.mbs.springframework.txnmgr.participant.CheckCurrency">
					<property name="txOk" value="Financial_Suite2"
						selectCriterion="checked" />
					<property name="txNok" value=""
						selectCriterion="unchecked" />
				</participant>
			</group>
