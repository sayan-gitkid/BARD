<g:if test="${'edit'.equals(subTemplate)}">
    <div class="row-fluid">
        <div class="span12">
            <g:link controller="context" action="addCard"
                    params="[contextClass: contextOwner.class.simpleName + 'Context', ownerId: contextOwner.id, cardSection: currentCard.key]"
                    class="btn"><i class="icon-plus"></i>Add Card</g:link>
            <br/><br/>
        </div>
    </div>
</g:if>
<g:if test="${currentCard || (subTemplate.equals("edit") && contextOwner instanceof bard.db.registration.Assay)}">
    <g:if test="${currentCard?.value}">
        <div id="cardHolder" class="span12">
            <div class="row-fluid">
                <g:each in="${contextOwner.splitForColumnLayout(currentCard.value)}" var="contextColumnList">
                    <div class="span6">
                        <g:each in="${contextColumnList}" var="context">
                            <g:if test="${showCheckBoxes}">
                            %{--for the case where we want to display only the contexts with at least one context item that has a non fixed attribute--}%
                                <g:if test="${context.atLeastOneNonFixedContextItem()}">
                                    <g:render template="../contextItem/${subTemplate}"
                                              model="[
                                                      contextOwner: contextOwner,
                                                      context: context,
                                                      cardSection: currentCard.key,
                                                      showCheckBoxes: showCheckBoxes,
                                                      existingContextIds: existingContextIds
                                              ]"/>
                                </g:if>

                            </g:if>
                            <g:else>
                                <g:render template="../contextItem/${subTemplate}"
                                          model="[contextOwner: contextOwner,
                                                  context: context,
                                                  cardSection: currentCard.key,
                                                  showCheckBoxes: showCheckBoxes,
                                                  existingContextIds: existingContextIds]"/>
                            </g:else>
                        </g:each>
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>
</g:if>