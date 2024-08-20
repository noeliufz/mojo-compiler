	.data
	.balign 8
Override2.A:
	.quad Override2.P
	.data
	.balign 8
Override2.B:
	.quad Override2.Q
	.data
	.balign 8
Override2.C:
	.quad Override2.P
	.quad Override2.Q
	.text
Override2:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
