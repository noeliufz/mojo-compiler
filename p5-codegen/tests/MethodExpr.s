	.data
	.balign 8
MethodExpr.T:
	.quad badPtr
	.text
MethodExpr:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
