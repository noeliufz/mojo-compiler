	.data
	.balign 8
AssignRecord.a:
	.zero 16
	.text
AssignRecord:
	pushq %rbp
	movq %rsp,%rbp
	subq $48,%rsp
	movq %rbx, -24(%rbp)
L.0:
	xorq %rax,%rax
	movq %rax,-16(%rbp)
	xorq %rax,%rax
	movq %rax,-8(%rbp)
	leaq AssignRecord.a(%rip),%rbx
	movq $1,%rax
	movq %rax,0(%rbx)
	leaq AssignRecord.a(%rip),%rbx
	movq $2,%rax
	movq %rax,8(%rbx)
	movq %rbp,%rax
	addq $-16,%rax
	movq %rax,%rdi
	leaq AssignRecord.a(%rip),%rax
	movq %rax,%rsi
	movq $16,%rax
	movq %rax,%rdx
	call memcpy
#	movq %rax,%rax
	movq -16(%rbp),%rax
	movq -8(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq %rax,%rdi
	call exit
#	movq %rax,%rax
#	returnSink
	movq -24(%rbp),%rbx
	addq $48,%rsp
	popq %rbp
	ret
AssignRecord.badSub:
	call badSub
