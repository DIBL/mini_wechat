#!/usr/bin/perl

use Parallel::ForkManager;
my $MAX_PROCESSES = 50;
my $pm = Parallel::ForkManager->new($MAX_PROCESSES);

my %port = get_port();
my %password = get_password();
my @names = ('Shuai Ni', 'Yifu Sun', 'Zhi Xu', 'Yang Tian', 'Wenji Liu', 'Shuai Hu', 'Fangshi Li', 'Feiyi Wang', 'Oulu Xu', 'Jing Li');
my $names_len = scalar @names;
my $msgCount = 100;

# Launch client server
for (my $i = 0; $i < $names_len; $i += 1) {
	system qq{mvn exec:java -D exec.mainClass="com.Elessar.app.SetupClientMain" -D exec.args="$port{$names[$i]}" &};
}

sleep(5);

# Total message sent is $msgCount x Combination(n, 2)
for (my $i = 0; $i < $names_len; $i += 1) {
	for (my $j = $i + 1; $j < $names_len; $j += 1) {
		my $pid = $pm->start and next;

		system qq{mvn exec:java -D exec.mainClass="com.Elessar.app.PerfTestMain" -D exec.args="'$names[$i]' $password{$names[$i]}, $port{$names[$i]} '$names[$j]' $password{$names[$j]}, $port{$names[$j]} $msgCount"};

		$pm->finish;
	}
}

sub get_port {
	return (
		"Shuai Ni" => 2000,
		"Yifu Sun" => 2500,
		"Zhi Xu" => 3000,
		"Yang Tian" => 3500,
		"Wenji Liu" => 4000,
		"Shuai Hu" => 4500,
		"Fangshi Li" => 5000,
		"Feiyi Wang" => 5500,
		"Oulu Xu" => 6000,
		"Jing Li" => 6500
	);
}

sub get_password {
	return (
		"Shuai Ni" => "shuaini",
		"Yifu Sun" => "yifusun",
		"Zhi Xu" => "zhixu",
		"Yang Tian" => "yangtian",
		"Wenji Liu" => "wenjiliu",
		"Shuai Hu" => "shuaihu",
		"Fangshi Li" => "fangshili",
		"Feiyi Wang" => "feiyiwang",
		"Oulu Xu" => "ouluxu",
		"Jing Li" => "jingli"
	);
}
