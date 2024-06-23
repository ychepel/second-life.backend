<#include "master_mail.ftlh">

<#macro title>Better luck next time!</#macro>
<#macro greeting>Dear ${name}!</#macro>
<#macro maintext>You participated in the auction stage of the <a href="${link}">offer</a> and the offer owner has chosen another participant as winner. You can search another offer on our marketplace with this  <a href="${active-offers}">link</a></#macro>

<@content/>
