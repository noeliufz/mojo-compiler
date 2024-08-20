	.data
	.balign 8
MethodCalls.A:
	.quad badPtr
	.data
	.balign 8
MethodCalls.AB:
	.quad badPtr
	.quad badPtr
	.data
	.balign 8
MethodCalls.T1:
	.quad badPtr
	.quad MethodCalls.Pab
	.data
	.balign 8
MethodCalls.T2:
	.quad MethodCalls.Pa
	.data
	.balign 8
MethodCalls.T3:
	.quad badPtr
	.quad MethodCalls.Pa
	.text
MethodCalls:
	pushq %rbp
	movq %rsp,%rbp
	subq $48,%rsp
	movq %r15, -40(%rbp)
	movq %r14, -32(%rbp)
	movq %r13, -24(%rbp)
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
L.0:
	xorq %rbx,%rbx
	xorq %r12,%r12
	xorq %r13,%r13
	xorq %r14,%r14
	xorq %r15,%r15
	movq -8(%rbx),%rax
	movq 0(%rax),%rax
	movq %rbx,%rdi
	call *%rax
	movq %rax,t.0
	leaq MethodCalls.A(%rip),%rax
	movq 0(%rax),%rax
	movq %rbx,%rdi
	call *%rax
	movq %rax,t.1
	movq -8(%r12),%rax
	movq 8(%rax),%rax
	movq %r12,%rdi
	call *%rax
	movq %rax,t.2
	leaq MethodCalls.AB(%rip),%rax
	movq 8(%rax),%rax
	movq %r12,%rdi
	call *%rax
	movq %rax,t.3
	movq -8(%r13),%rax
	movq 8(%rax),%rax
	movq %r13,%rdi
	call *%rax
	movq %rax,t.4
	leaq MethodCalls.T1(%rip),%rax
	movq 8(%rax),%rax
	movq %r13,%rdi
	call *%rax
	movq %rax,t.5
	movq -8(%r14),%rax
	movq 0(%rax),%rax
	movq %r14,%rdi
	call *%rax
	movq %rax,t.6
	leaq MethodCalls.T2(%rip),%rax
	movq 0(%rax),%rax
	movq %r14,%rdi
	call *%rax
	movq %rax,t.7
	movq -8(%r15),%rax
	movq 8(%rax),%rax
	movq %r15,%rdi
	call *%rax
	movq %rax,t.8
	leaq MethodCalls.T3(%rip),%rax
	movq 8(%rax),%rax
	movq %r15,%rdi
	call *%rax
	movq %rax,t.9
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	movq -24(%rbp),%r13
	movq -32(%rbp),%r14
	movq -40(%rbp),%r15
	addq $48,%rsp
	popq %rbp
	ret
